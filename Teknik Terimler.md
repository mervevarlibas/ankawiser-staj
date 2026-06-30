# Geliştirme
**Unit Test** (birim test), bir yazılımın en küçük, bağımsız test edilebilir parçasının (genelde tek bir fonksiyon, metod veya sınıf) beklenen şekilde çalışıp çalışmadığını, diğer bileşenlerden izole bir şekilde doğrulayan otomatik testtir.

izolasyon: Unit test, test edilen birimi dış bağımlılıklarından (veritabanı, network isteği, dosya sistemi, başka servisler) bağımsız olarak test eder. Bu bağımlılıklar genelde mock, stub veya fake nesnelerle taklit edilir. Amaç: testin yavaş, kararsız (flaky) veya dış sistemlere bağımlı olmaması; sadece o fonksiyonun mantığının doğruluğunu kontrol etmesi.

AAA Pattern (yaygın yapı):
Arrange: Test için gerekli veriyi/durumu hazırla.
Act: Test edilen fonksiyonu çağır.
Assert: Sonucun beklenenle eşleştiğini doğrula

**Integration Test** (entegrasyon testi), birden fazla bileşenin/modülün birlikte doğru çalışıp çalışmadığını test eden, unit testin aksine izolasyon yerine gerçek (veya gerçeğe yakın) etkileşimleri kontrol eden test türüdür.

Gerçek MongoDB Atlas'a bağlanmak yerine, her test çalıştırmasında RAM üzerinde geçici bir MongoDB instance'ı ayağa kaldırılır. Böylece:
Testler production/gerçek veriyi kirletmez.
Testler internet bağlantısına bağımlı olmaz 
Her test temiz bir veritabanı durumuyla başlar.

Test izolasyonu (her test bağımsız olmalı): Bir testin verisi diğerini etkilememeli — genelde her test öncesi/sonrası veritabanı temizlenir

Yavaşlık: Integration testler unit testlere göre daha yavaş çalışır (gerçek dosya işlemi var)

Integration testler genelde CI pipeline'ında (GitHub kod gonderdiginde) her pull request'te otomatik çalıştırılır — kod birleştirilmeden önce sistemin bütün halinde bozulmadığını garanti eder.

**Refactoring** (yeniden düzenleme), bir yazılımın dışarıdan gözlemlenen davranışını değiştirmeden, iç yapısını/kodunu daha okunabilir, sürdürülebilir ve geliştirilebilir hale getirmek amacıyla yeniden organize etme sürecidir.
Bug fix: hatalı davranışı düzeltir
Yeni özellik: sisteme yeni özellik ekler
Refactoring: davranış aynı kalır, sadece kod kalitesi artar.

Bu yüzden refactoring yaparken testlerin (özellikle unit ve integration testlerin) önceden var olması önemlidir — testler "davranış gerçekten değişmedi mi?" sorusunu garanti eden güvenlik ağıdır

**Technical Debt** (teknik borç), Hızlı bir çözümün, daha doğru ama zaman alan çözüm yerine seçilmesi sonucu oluşan ve gelecekte ek zaman, emek ve hata riski oluşturan yazılım borcudur.

Tipik kaynakları:
Deadline baskısı: "Şimdi çalışsın, sonra düzeltiriz" denip hiç düzeltilmemesi.
Yetersiz test coverage: Test yazmadan hızlı kod yazmak (kısa vadede hızlı, uzun vadede her değişiklikte hata riski).
Eksik dökümantasyon: Kodun neden öyle yazıldığının kaydedilmemesi, gelecekte aynı hatanın tekrarlanması.
Eski/deprecated bağımlılıklar: Güncellenmeyen kütüphaneler, framework versiyonları

Nasıl yönetilir:

Backlog'a kaydetmek: Yapılan geçici çözümü (quick fix) daha sonra düzeltmek için bir ticket/issue olarak kayıt altına almak
Boy Scout Rule: Refactoring bölümünde bahsettiğimiz gibi, dokunulan kodu biraz daha temiz bırakmak.
Düzenli "debt payoff" sprintleri: Bazı ekipler her sprint'in belirli bir yüzdesini (örn. %10-20) sadece teknik borç ödemeye ayırır.

**Debugging**  bir yazılımda beklenmeyen davranışa (bug) neden olan kök sebebi (root cause) sistematik olarak tespit edip düzeltme sürecidir.

Bug kategorileri:
Syntax error: Kod derlenmez/çalışmaz bile (en kolay tespit edilir) kodun yazım kuralları hatası
Logic error: Kod çalışır ama yanlış sonuç üretir 
Runtime error: Çalışma sırasında oluşan hata (tanımlı olmayan fonksiyon çağrısı).
Race condition: Eşzamanlı (asenkron) işlemlerin sırasına bağlı, tutarsız sonuç veren hatalar 

**Agile**  yazılım geliştirmede uzun, katı planlı süreçler yerine; kısa, yinelemeli döngülerle çalışmayı, sürekli geri bildirim almayı ve değişime hızlı uyum sağlamayı önceliklendiren bir yazılım geliştirme ailesidir.

Waterfall: Gereksinimler baştan tamamen belirlenir → tasarım → geliştirme → test → teslim, sırayla ve geri dönüşsüz aşamalar. Değişiklik geç fark edilirse maliyeti çok yüksektir.
Agile: Kısa döngülerde (genelde 1-4 hafta, "sprint" denir) küçük, çalışan parçalar teslim edilir; her döngü sonunda geri bildirim alınır ve sonraki döngü buna göre şekillenir.

**Scrum**
Agile altındaki en yaygın kullanılan çerçevedir (framework); karmaşık ürün geliştirme problemlerini, sabit süreli yinelemeli döngüler (sprint), tanımlı roller, belirli toplantılar (event/ceremony) ve somut çıktılar (artifact) aracılığıyla yönetilebilir hale getirir. Agile bir felsefeyse, Scrum o felsefeyi uygulamayan yöntem.

Scrum'ın 3 sütunu:
Transparency (şeffaflık): Sürecin her aşaması, ilgili herkes tarafından görülebilir olmalı 
Inspection (denetim): İlerleme düzenli aralıklarla gözden geçirilir (daily standup, sprint review gibi toplantılar yapılır).
Adaptation (uyarlama): Denetim sonucunda sapma fark edilirse süreç hızlıca düzeltilir.

**Sprint** Scrum çerçevesinde kullanılan, sabit süreli (time-boxed) çalışma döngüsüdür; genelde 1 ile 4 hafta arasında sürer ve sonunda potansiyel olarak teslim edilebilir bir ürün artırımı ortaya çıkar. 

Sprint süresi baştan sabittir ve değiştirilmez — eğer planlanan iş bitmezse sprint uzatılmaz, bitmeyen iş bir sonraki sprint'in backlog'una geri döner

Sprint Goal: Her sprint'in tek, net bir hedefi olmalı (örn. "Bu sprint sonunda kullanıcı kayıt/giriş akışı çalışır durumda olacak"). Tüm sprint backlog bu hedefe hizmet eder.
Sprint içinde kapsam sabittir: Sprint başladıktan sonra, Development Team'in üzerinde anlaştığı işler değiştirilmez/eklenmez (Product Owner yeni bir öncelikli iş isterse, bu mevcut sprint'i bozmadan bir sonraki sprint'e planlanır). 
Bitmeyen iş geri döner: Sprint sonunda tamamlanmayan işler otomatik olarak sonraki sprint'e geçmez. Product Backlog'a geri döner, tekrar önceliklendirilir ve yeniden planlanır.

**Framework** belirli bir alannda yazılım geliştirmeyi hızlandırmak ve standartlaştırmak için önceden tasarlanmış, yeniden kullanılabilir kod altyapısı, kurallar bütünü ve araçlar setidir. Geliştiriciye "sıfırdan başlama" yerine, üzerinde geliştirebileceği hazır bir iskelet sunar.

Library (kütüphane): Kontrol geliştiricide. Sen çağırırsın. Mongoose, Axios, Lodash birer kütüphanedir — istediğinde import edip istediğin yerde çağırırsın.
Framework: Framework çağırır. Kontrol framework'te
Senin kodun framework'ün kurallarına, yapısına, yaşam döngüsüne uyar.

Web backend framework:Sunucu tarafında tekrar eden işleri kolaylaştırır. HTTP isteklerini karşılama, routing, middleware, authentication gibi tekrarlayan backend ihtiyaçlarını soyutlar.
Frontend framework/library: UI bileşenlerini, state yönetimini, DOM manipülasyonunu yönetir. Örnekler:React
Mobile framework: Mobil uygulama geliştirmeyi soyutlar:
React Native

**Open Source** (açık kaynak), kaynak kodunun herkese açık, herkes tarafından incelenebilir, değiştirilebilir ve dağıtılabilir şekilde yayınlandığı yazılım geliştirme modelidir. 

Closed source: Kaynak kodu gizlidir, sadece çalıştırılabilir (binary) hali dağıtılır — nasıl çalıştığını göremezsin, değiştiremezsin. Örnek: Microsoft Windows, Adobe Photoshop