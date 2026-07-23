package com.mwitter.service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;//Java'nın JMX (Java Management Extensions) API'si. JVM'in kendi iç durumunu (bellek, thread, runtime) sorgulamak için kullanılır.
import java.net.InetAddress;//Sunucunun ağ üzerindeki host bilgisine (hostname) erişmek için.
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;//Disk alanı bilgisi (toplam/boş alan) almak için kullanılan dosya sistemi API'leri.
import java.time.Instant;//Metriklerin alındığı anlık zaman damgası için

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;//MongoDB'ye ping atıp durumunu kontrol etmek için gerekli sınıflar.
import org.springframework.stereotype.Service;//Bu sınıfın bir Spring "Service" bean'i olduğunu belirten anotasyon.

import com.mwitter.dto.AdminMetricsResponse;
import com.mwitter.monitoring.HttpRequestMetrics;//Projeye özgü sınıflar: sonuç DTO'su ve HTTP istek metriklerini tutan özel bir bileşen.

@Service
public class AdminMetricsService {//Spring'e bu sınıfı otomatik olarak bean olarak kaydettirir.başka yerlerde kullanılabilir

    private final MongoTemplate mongoTemplate;//MongoDB ile iletişim için.
    private final HttpRequestMetrics requestMetrics;//HTTP isteklerinin (toplam, başarısız, hata oranı, ort. yanıt süresi) tutulduğu özel bileşen.
    private final com.sun.management.OperatingSystemMXBean operatingSystem;//JVM'in işletim sistemi seviyesindeki bilgilere (CPU, RAM) eriştiği özel bir arayüz

    public AdminMetricsService(MongoTemplate mongoTemplate, HttpRequestMetrics requestMetrics) {//Spring, MongoTemplate ve HttpRequestMetrics'i constructor injection ile otomatik sağlar.
        this.mongoTemplate = mongoTemplate;
        this.requestMetrics = requestMetrics;
        this.operatingSystem = ManagementFactory.getPlatformMXBean(//operatingSystem ise dışarıdan enjekte edilmiyor; ManagementFactory üzerinden manuel alınıyor.çünkü bu bir Spring bean'i değil, JVM'in kendi sağladığı bir platform MXBean'i.
                com.sun.management.OperatingSystemMXBean.class);
    }

    public AdminMetricsResponse currentMetrics() {
        MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();//JVM heap belleğinin kullanım bilgisini alır (kullanılan/maksimum bellek).
        var threads = ManagementFactory.getThreadMXBean();//aktif thread sayısı, zirve (peak) thread sayısı gibi bilgiler için.
        var runtime = ManagementFactory.getRuntimeMXBean();//JVM'in ne zamandır çalıştığı (uptime) gibi bilgiler için.
        HttpRequestMetrics.Snapshot http = requestMetrics.snapshot();//O ana kadar toplanan HTTP istek istatistiklerinin bir "anlık görüntüsünü

        long totalMemory = operatingSystem == null ? -1 : operatingSystem.getTotalMemorySize();
        long freeMemory = operatingSystem == null ? -1 : operatingSystem.getFreeMemorySize();
        long usedMemory = totalMemory < 0 || freeMemory < 0 ? -1 : totalMemory - freeMemory;//Sistem genelindeki (sadece JVM değil, tüm makine) toplam ve boş RAM'i alır. operatingSystem bulunamazsa -1 ile "veri yok" işaretlenir. usedMemory de aynı mantıkla hesaplanır

        long diskTotal = -1;
        long diskUsed = -1;
        try {
            FileStore store = Files.getFileStore(Path.of(".").toAbsolutePath());//Uygulamanın çalıştığı dizinin ("." = mevcut dizin) bulunduğu disk bölümünü (FileStore) bulur, toplam ve kullanılan alanı hesaplar.
            diskTotal = store.getTotalSpace();
            diskUsed = diskTotal - store.getUsableSpace();
        } catch (Exception ignored) {
            // Bazı container sağlayıcıları disk bilgisini sınırlar; panel "kullanılamıyor" gösterir.böyle bir durumda exception yutulur ve -1 değerleri kalır, panelde "kullanılamıyor" gösterilir.
        }

        String databaseStatus = databaseStatus();//Veritabanı durumunu kontrol eder veritabanı "UP" değilse "DEGRADED" (bozulmuş/kısmi çalışır) olarak işaretlenir
        String overallStatus = "UP".equals(databaseStatus) ? "UP" : "DEGRADED";

        return new AdminMetricsResponse(//Tüm toplanan verileri bir AdminMetricsResponse DTO'suna paketler
                Instant.now(),//metriklerin alındığı zaman
                overallStatus,
                databaseStatus,
                serverName(),//sunucu adı
                ProcessHandle.current().pid(),//mevcut process'in işletim sistemi PID'i
                Runtime.getRuntime().availableProcessors(),//kullanılabilir CPU çekirdek sayısı
                percent(operatingSystem == null ? -1 : operatingSystem.getCpuLoad()),//Sunucu veya container genelindeki CPU kullanımını verir.
                percent(operatingSystem == null ? -1 : operatingSystem.getProcessCpuLoad()),//Yalnızca çalışan Java/Mwitter işleminin CPU kullanımını verir.
                usedMemory,
                totalMemory,//sistem RAM'i
                heap.getUsed(),
                heap.getMax(),//JVM heap kullanım
                diskUsed,
                diskTotal,//disk kullanımı
                runtime.getUptime() / 1000,//JVM'in kaç saniyedir ayakta olduğu (milisaniyeden saniyeye çevrilir)
                threads.getThreadCount(),
                threads.getPeakThreadCount(),//anlık ve zirve thread sayısı
                http.totalRequests(),
                http.failedRequests(),
                http.errorRatePercent(),
                http.averageResponseMs());//HTTP trafiği istatistikleri
    }

    private String databaseStatus() {//MongoDB'ye standart ping komutunu gönderir. Mongo, başarılı yanıtta ok: 1.0 döner. Bu değer 1.0 ise "UP", değilse DOWN döner.Bu, veritabanının canlı/erişilebilir olup olmadığını anlamanın standart bir yoludur.
        try {
            Document result = mongoTemplate.executeCommand("{ ping: 1 }");
            Object ok = result.get("ok");
            return ok instanceof Number number && number.doubleValue() >= 1.0
                    ? "UP"
                    : "DOWN";
        } catch (Exception exception) {
            return "DOWN";
        }
    }

    private static String serverName() {//Sunucunun/instance'ın adını bulmaya çalışır
        for (String variable : new String[] { "RENDER_INSTANCE_ID", "HOSTNAME", "COMPUTERNAME" }) {//sırayla
            String value = System.getenv(variable);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        try {
            return InetAddress.getLocalHost().getHostName();//Bunların hiçbiri set edilmemişse, işletim sisteminden host adını almaya çalışır.
        } catch (Exception exception) {//O da başarısız olursa "unknown" döner.
            return "unknown";
        }
    }

    private static double percent(double ratio) {
        return ratio < 0 ? -1 : Math.round(ratio * 1000.0) / 10.0;//Eğer oran negatifse (veri yok) -1 döndürür Değilse, oranı yüzdeye çevirir
    }
}
