# API & GUVENLIK

**API**  iki farklı yazılımın veya uygulamanın birbiriyle haberleşmesini ve veri alışverişi yapmasını sağlayan kurallar bütünüdür.

 Web geliştirme, Mobil uygulamalar,Mikroservis mimarisinde kullanılır.

**REST**  istemci  ile sunucu arasında HTTP üzerinden veri alışverişi yapılmasını sağlayan bir mimari stildir. API'lerin nasıl tasarlanması gerektiğine dair kurallar ve prensipler sunar.API geliştirmek için kullanılan bir mimari yaklaşımdır.

REST'te en sık kullanılan HTTP metodlar GET, POST, PUT, DELETE, PATCH.

**GraphQL**
istemcinin ihtiyaç duyduğu veriyi tam olarak istediği şekilde sunucudan almasını sağlayan bir API sorgulama dili  ve bu sorguları çalıştıran bir çalışma zamanıdır.

REST'ten farklı olarak istemci, hangi alanları istediğini kendisi belirtir. Böylece gereksiz veri gönderilmez.

Verileri ilişkisel bir grafik gibi düşünerek sorgulamaya olanak tanır. İstemci, bu grafın hangi düğümlerini ve hangi alanlarını istediğini belirtir.

**Endpoint** bir API'de istemcinin belirli bir kaynağa veya işleme erişmek için istek gönderdiği URLdir.API'nin dış dünyaya açılan erişim noktasıdır.İstemcinin isteğinin ulaştığı son adresi ifade eder.

REST API,GraphQL API,Mikroservis mimarilerinde kullanılır.

https://api.example.com/users/5 URL'nin /users/5 kısmı ilgili kaynağın endpoint'ini ifade eder.Bir API'nin birçok endpoint'i olabilir.

**Authentication**
(Kimlik Doğrulama), sisteme erişmeye çalışan kullanıcının veya uygulamanın gerçekten iddia ettiği kişi veya sistem olup olmadığını doğrulama işlemidir.

Kullanıcı adı + şifre,Parmak izi,Yüz tanıma,SMS doğrulama kodu bunların hepsi kimlik doğrulama yöntemlerindendir.

Başarılı olursa sunucu genellikle bir token(sunucunun başarılı giriş yaptıktan sonra kullanıcıya verdiği geçici bir dijital kimlik kartı gibidir) döndürür. Sonraki isteklerde kullanıcı bu token ile kimliğini kanıtlar. Yani her istekte kullanıcı adı ve şifresini tekrar göndermek zorunda kalmaz.

**Authorization** (Yetkilendirme), kimliği doğrulanmış bir kullanıcının veya sistemin hangi kaynaklara erişebileceğini ve hangi işlemleri yapabileceğini belirleme işlemidir.

Kullanıcı giriş yaptıktan sonra sunucudan bir token almış olsun.Sunucu önce
Token geçerli mi? sonra Bu kullanıcının kullanıcı silme yetkisi var mı?kullanıcı admin ise:
200 OK,Değilse: 403 Forbidden

**JWT**
JSON Web Token (JWT), kullanıcının kimliğini ve bazı bilgilerini güvenli bir şekilde taşımak için kullanılan, dijital olarak imzalanmış bir token formatıdır.
En çok authentication ve authorization işlemlerinde kullanılır.Web üzerinde JSON formatında bilgi taşıyan dijital belirteç.

JWT 3 bölümden oluşur:Header, token hakkında bilgiler içerir.

alg → Hangi imzalama algoritmasının kullanıldığı.
typ → Token türünün JWT olduğu.

Payload, kullanıcıyla ilgili bilgileri taşır.Burada Kullanıcı ID'si,Kullanıcı adı,Rolü
gibi bilgiler bulunabilir.

Signature (İmza), JWT'nin değiştirilmediğini doğrulamak için oluşturulur.Eğer biri payload'ı değiştirirse imza artık geçerli olmaz ve sunucu bu JWT'yi reddeder.

**SDK** belirli bir platform, dil veya teknoloji için uygulama geliştirmeyi kolaylaştıran araçlar, kütüphaneler, dokümantasyonlar ve örnek kodlardan oluşan geliştirme paketidir.

Android SDK, Java SDK, Amazon Web Services SDK gibi çeşitleri vardır.

Araç + kod + doküman sağlar.