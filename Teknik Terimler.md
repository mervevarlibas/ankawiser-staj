# Altyapı
**CI/CD** 
yazılım geliştirme sürecinde kod değişikliklerinin otomatik olarak test edilmesi, hazırlanması ve yayınlanmasını sağlayan yazılım geliştirme yaklaşımıdır.CI geliştiricilerin yazdığı kodları sık sık ortak projeye eklemesi ve bu kodların otomatik kontrol edilmesidir. CD 2 farklı olabilir. Birincisi Kod hazır hale getirilir ama yayına alma kararı insan tarafından verilebilir.İkincisi her şey başarılıysa sistem otomatik olarak yayına çıkar.

CI/CD, DevOps kültürünün bir parçasıdır. Amaç Daha hızlı geliştirme, daha az hata,daha sık güncelleme

**DevOPS** 
yazılım geliştirme ve sistem operasyon ekiplerinin birlikte çalışmasını sağlayan yaklaşımdır. Amaç yazılımı daha hızlı, güvenilir ve otomatik şekilde geliştirmek, test etmek ve yayınlamaktır. Bir yazılımın yaşam döngüsünü otomatikleştirir.
Kod yaz
  ↓
Git'e gönder
  ↓
Test et (CI)
  ↓
Build al
  ↓
Sunucuya gönder (CD)
  ↓
İzle
Kullandığı araçlar Git, Jenkinss, Docker

**Docker** 
uygulamaları ve çalışması için gereken tüm bağımlılıkları (kütüphane,ayarlar vb.) bir paket haline getirip container (konteyner) adı verilen izole ortamlarda çalıştırmayı sağlayan bir platformdur.Her container kendi ortamında çalışır. Docker kodun çalışması için gereken ortamı da kodla beraber taşır.Mesela proje+node.js+mongoDB+gerekli paketler+ayarlar=container

**Kubernetes** 
çok sayıda container'ı yönetmek, çalıştırmak, ölçeklendirmek ve kontrol etmek için kullanılan bir container orchestration (konteyner orkestrasyon) platformudur. Docker kutuları oluşturur, Kubernetes binlerce kutuyu yönetir.

Otomatik ölçeklendirme yapabilir mesela 3 backend container var ama kullanıcı arttı 10 backend container yapabilir. Sonra bunu düşürebilir.

**Cloud** bilgisayar kaynaklarının (sunucu, depolama, veritabanı, ağ, yazılım vb.) internet üzerinden ihtiyaç olduğunda kullanılmasını sağlayan bilişim modelidir. Docker uygulamayı paketler. Cloud o paketi internet üzerindeki sunucularda çalıştırır. Kubernetes cloud üzerindeki binlerce docker container i yönetir.

**Server**
başka bilgisayarlara veya cihazlara hizmet sağlayan bilgisayardır. Diğer cihazların isteklerini karşılar. İstemci istek atar sunucu alır cevabı istemciye iletir. Server web sitesi dosyaları, veritabanı, kullanıcı bilgileri, API tutabilir.

tarayıcı->web server->html/css/js
backend->database server->kullanıcı bilgisi
application server->uygulama kodunu çalıştırır(node.js backendin çalıştığı yer)

Eskiden şirketin kendi fiziksel server ı vardı şimdi cloud üzerinde server kiralanabiliyor.

**Load Balancer**
gelen kullanıcı isteklerini birden fazla sunucuya dağıtan sistemdir.Amaç tek bir sunucunun aşırı yüklenmesini önlemek, performansı arttırmak.Load Balancer sadece dağıtmaz, kontrol de eder.Örneğin API1 çöktü load balancer API2 ve API3 tarafına dağıtır yeni kullanıcıları. Buna yüksek erişilebilirlik denir.

**Cache**
daha önce alınmış veya hesaplanmış verileri daha hızlı erişmek için geçici olarak saklayan bellek alanıdır. Veriyi saklar ama normal depolama gibi kalıcı değildir.Amaç veriyi güvenli bir yerde tutmak değil ona hızlı ulaşmaktır.

Mesela bir uygulama kullanıcı profili istiyor. İlk seferde uygulama->database->kullanıcı bilgisi. Ama tekrar istenirse uygulama ->cache->kullanıcı bilgisi.

Örnek araçlar redis, memcached.(genelde backend sistemlerinde cache için kullanılır)

**CDN**
web sitelerindeki statik içerikleri (resim, video, CSS, JavaScript, dosya vb.) kullanıcıya en yakın sunucudan gönderen dağıtık sunucu ağıdır.Tek bir sunucu yerine birçok farklı noktadaki sunucular kullanılır.

**Deployment** geliştirilen bir yazılımın kullanıcıların erişebileceği bir ortama yüklenmesi ve çalışır hale getirilmesi işlemidir. Proje yaptın sadece senin bilgisayarında çalışıyor. Deployment yapınca kod-> cloud server ->www.site.com->herkes kullanabilir. Adımları birinci olarak build alma(kod üretime hazırlanır),sunucuya gönderme(dosyalar server a aktarılır),çalıştırma(uygulama başlatılır)

**Environment**
(ortam), bir yazılımın çalıştığı donanım, işletim sistemi, yazılım ayarları, değişkenler ve bağımlılıkların bulunduğu çalışma koşullarıdır. Uygulamayı çevreleyen tüm koşullar.-