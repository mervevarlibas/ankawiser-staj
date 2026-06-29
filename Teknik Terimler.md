# Staj Terimleri Listesi
## Mimari
- **Backend**
Bir uygulamanın sunucu tarafında çalışan, kullanıcının doğrudan görmediği ancak uygulamanın temel işleyişini sağlayan yazılım katmanıdır. İş mantığının yürütülmesi, veritabanı işlemlerinin gerçekleştirilmesi, kullanıcı kimlik doğrulama ve yetkilendirme süreçlerinin yönetilmesi, API'lerin geliştirilmesi ve frontend ile veri alışverişinin sağlanması backend'in temel görevleridir.

  Backend, istemciden gelen HTTP isteklerini işler, gerekli kontrolleri yapar, veritabanı ile iletişim kurar ve uygun HTTP yanıtını döndürür.

  Backend geliştirmede kullanılan yaygın programlama dilleri arasında Java, JavaScript (Node.js), C#, Python ve Go bulunur. Node.js, JavaScript kodunun tarayıcı dışında, sunucu tarafında çalışmasını sağlayan bir çalışma ortamıdır.
Backend geliştirme sürecini kolaylaştırmak amacıyla framework'ler kullanılır, birçok hazır bileşen sunarak geliştirme sürecini hızlandırır. Örneğin Express.js, Node.js üzerinde çalışan hafif bir web framework'üdür
- **Frontend**
bir uygulamanın istemci tarafında çalışan, kullanıcının doğrudan gördüğü ve etkileşime geçtiği yazılım katmanıdır. Arayüzün (UI) oluşturulmasından sorumludur. Butonlar, formlar, menüler, sayfa düzeni, animasyonlar ve kullanıcı etkileşimleri frontend tarafında geliştirilir. Backend'den gelen verileri işleyerek kullanıcıya görsel ve etkileşimli bir arayüz sunar.

  Frontend geliştirmenin temel teknolojileri HTML, CSS ve JavaScript'tir. HTML sayfanın yapısını, CSS görsel tasarımını, JavaScript ise sayfanın davranışını ve kullanıcı etkileşimlerini sağlar.

  Modern frontend geliştirmede en yaygın kullanılan teknolojilerden biri React'tir. React, kullanıcı arayüzü geliştirmek için kullanılan JavaScript tabanlı bir UI kütüphanesidir. Temel felsefesi, büyük ve karmaşık kullanıcı arayüzlerini küçük, bağımsız ve yeniden kullanılabilir component'lere ayırarak geliştirmektir. Component, belirli bir görevi yerine getiren, bağımsız çalışabilen ve uygulamanın farklı bölümlerinde tekrar kullanılabilen bir JavaScript fonksiyonudur. Bu yapı kodun okunabilirliğini, bakımını ve yeniden kullanılabilirliğini artırır.

  React'in en önemli özelliklerinden biri Virtual DOM mekanizmasıdır. Tarayıcıda bulunan gerçek DOM üzerinde doğrudan işlem yapmak maliyetli olduğu için React, bellekte (RAM) gerçek DOM'un hafif bir kopyasını oluşturur. Uygulamada bir değişiklik olduğunda React, yeni Virtual DOM ile önceki Virtual DOM'u karşılaştırır. Bu karşılaştırma işlemine Diffing Algorithm denir. Karşılaştırma sonucunda yalnızca değişen düğümler tespit edilir ve sadece bu değişiklikler gerçek DOM'a uygulanır. Virtual DOM ile Real DOM arasındaki karşılaştırma ve güncelleme sürecinin tamamına ise Reconciliation adı verilir. Bu mekanizma sayesinde gereksiz DOM manipülasyonları önlenir, sayfanın tamamı yeniden render edilmez ve uygulamanın
  performansı önemli ölçüde artırılır
- **Full Stack**
bir yazılım uygulamasının hem frontend hem de backend geliştirme süreçlerini kapsayan yaklaşımdır.
En yaygın Full Stack teknoloji yığınlarından biri MERN Stack'tir(MongoDB,Express.js,React,Node.js)
- **Cross Platform**
tek bir kaynak kodu kullanılarak bir uygulamanın birden fazla işletim sistemi için geliştirilebilmesini sağlayan yazılım geliştirme yaklaşımıdır. Bu yaklaşım sayesinde aynı kod tabanı kullanılarak Android, iOS, Windows ve macOS gibi farklı platformlarda çalışan uygulamalar geliştirilebilir.

  Native uygulama geliştirmede her platform için farklı programlama dilleri ve araçlar kullanılır(Android uygulamaları genellikle Java veya Kotlin, iOS uygulamaları Swift veya Objective-C, Windows uygulamaları ise C# ile geliştirilir)

  Cross Platform yaklaşımında kullanılan framework, bu kodu hedef platformun anlayacağı native bileşenlere veya platforma uygun çalıştırılabilir koda dönüştürür.

  Cross Platform geliştirmenin en yaygın teknolojileri şunlardır:
React Native(JavaScript/TypeScript),Flutter(Google tarafından),.NET MAUI(Microsoft tarafından)
- **Microservices**
büyük ve karmaşık bir uygulamanın, belirli iş alanlarından sorumlu, küçük ve birbirinden bağımsız servisler halinde tasarlanmasını sağlayan yazılım mimarisidir.Her servis tek bir görevi yerine getirir ve diğer servislerden bağımsız olarak geliştirilebilir
- **Monolith**
bir yazılım uygulamasının tüm bileşenlerinin (frontend, backend, iş mantığı, veri erişim katmanı vb.) tek bir uygulama içerisinde geliştirildiği ve dağıtıldığı yazılım mimarisidir. Uygulamanın bütün modülleri aynı kod tabanı içinde yer alır.Çoğunlukla tüm modüller aynı veritabanını kullanır.

  Modern projelerde tamamen klasik monolith yerine Modular Monolith yaklaşımı da kullanılır. Bu yapıda uygulama yine tek proje olarak kalır ancak kendi içinde bağımsız modüllere ayrılır. Böylece ileride Microservices mimarisine geçiş daha kolay hale gelir.
 - **Scalability**
 bir yazılım sisteminin artan kullanıcı sayısı, veri miktarı veya işlem yükü karşısında performansını koruyarak büyüyebilme yeteneğidir.Ölçeklenebilir bir sistem; daha fazla kullanıcıya hizmet verebilir, daha fazla veriyi işleyebilir ve performans kaybını minimumda tutabilir.

   1.Dikey Ölçekleme:
Mevcut sunucunun donanımsal olarak güçlendirilmesi yöntemidir.
  
   2.Yatay Ölçekleme:
Sisteme daha fazla sunucu ekleyerek kapasitenin artırılmasıdır.
Load Balancer, yatay ölçekleme mimarisinde kullanılan ve gelen kullanıcı isteklerini birden fazla sunucu arasında dengeli şekilde dağıtan sistem bileşenidir.
Scalability sadece sunucu tarafında değil; database tarafında da uygulanabilir.
 - **Agnostic**
 bir yazılım sisteminin belirli bir teknolojiye, platforma, veritabanına veya sağlayıcıya bağımlı olmadan farklı teknolojilerle çalışabilecek şekilde tasarlanması anlamına gelir.taşınabilir olması hedeflenir.
 1. Veritabanı Bağımsızlığı:
Bir uygulamanın belirli bir veritabanına bağlı olmadan farklı veritabanlarıyla çalışabilmesidir.
Bu yapı genellikle ORM araçları ile sağlanır.
2. Platform Bağımsızlığı:
Bir yazılımın farklı işletim sistemlerinde çalışabilmesidir.Java kodu doğrudan işletim sistemi üzerinde çalışmaz, Java Virtual Machine üzerinde çalışır.
3. Bulut Bağımsızlığı:
Bir uygulamanın belirli bir cloud sağlayıcısına bağımlı olmadan farklı bulut platformlarında çalışabilmesidir.Docker, uygulamayı ve bağımlılıklarını paketleyerek farklı ortamlarda aynı şekilde çalışmasını sağlar.