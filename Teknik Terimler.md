# VERİ
**Database** birbiriyle ilişkili verilerin organize, yapılandırılmış ve kalıcı bir şekilde depolandığı, üzerinde verimli sorgulama, ekleme, güncelleme ve silme işlemleri yapılabilen bir sistemdir. Database Managament System(DBMS) ise veritabanına erişimi, güvenliğini, depolanmasını yöneten yazılım katmanıdır. Kullanıcı veya uygulama doğrudan diskteki dosyalarla değil dbsm üzerinden veriyle etkileşime girer.(mysql,mongodb)

Veriler ilişkisel veya nosql şeklinde modelde olabilir. İlişkiselde veriler tablolar halinde tutulur. SQL kullanılır.

Sorgu performansını artırmak için Indexing veri yapısı kullanılır. Bir sütun üzerinde index oluşturmak o sütuna göre arama yaparken çok daha hızlı erişimi sağlar. İlişkisel veritabanında veri tekrarını azaltmak ve veri bütünlüğünü korumak için tabloların belli kurallara göre tasarlanmasına ise normalizasyon denir.

**NoSQL**
Not Only SQL. İlişkisel veritabanlarının katı şema ve tablo yapısına alternatif olarak geliştirilmiş genellikle yatay ölçeklenebilirlik ve esnek veri modelleri sunan veritabanı sistemleri kategorisidir.

Web ölçeğinde büyüyen sistemlerde dikey ölçeklendirme limitleerine takılması ve sabit şemanın hızlı değişen veri yapılarına uygun olmaması nedeniyle ortaya çıkmıştır.

1-Document Based:Veri,JSON benzeri dokümanlar halinde saklanır. Her doküman kendi içinde farklı alanlara sahip olabilir.

2-Key Value:Her veri bir anahtar ve ona karşılık gelen değer olarak tutulur.

3-Graph:Veriler düğüm ve kenar ilişkileri şeklinde tutulur.

SQL sabit şema iken NoSQL esnek bir şemaya sahiptir.SQL dikey ölçeklenir,NoSQL yatay ölçeklenir.

**SQL** (Structured Query Language) ilişkisel veritabanlarında veri tanımlama, sorgulama, güncelleme ve yönetme işlemleri için kullanılan standart bir sorgu dilidir.Hem bir dil hem de bu dili kullanan veritabanı sistemlerinin (MySQL, PostgreSQL, SQL Server, Oracle, SQLite) genel adı olarak da kullanılır

 SQL, verinin satır ve sütun içeren tablolarda tutulduğu, tablolar arası ilişkilerin primary key – foreign key mekanizmasıyla kurulduğu modele dayanır.En güçlü yanlarından biri, birden fazla tabloyu ortak bir sütun üzerinden birleştirebilmesidir(inner join yalnızca eşleşen kayıtları,left join,right join)

 **ORM** (Object-Relational Mapping), nesne yönelimli programlama dillerindeki (Java, Python, JavaScript/Node.js vb.) sınıflar/nesneler ile ilişkisel veritabanındaki tablolar arasında otomatik bir eşleme sağlayan tekniktir/kütüphanedir. Amaç, geliştiricinin ham SQL sorguları yazmak yerine, veritabanı işlemlerini programlama dilinin kendi nesneleri üzerinden yapabilmesidir.
 Bir tablo → bir sınıf (class)
Bir satır (row) → o sınıftan bir nesne (instance/object)
Bir sütun → nesnenin bir özelliği (attribute/field)

kod farklı veritabanı sistemlerine (MySQL'den PostgreSQL'e) daha kolay taşınabilir.işlemleri daha az kodla, daha hızlı yazılır.

**Migration**  (veritabanı göçü), veritabanı şemasının zaman içinde kontrollü, izlenebilir ve tekrarlanabilir bir şekilde değiştirilmesini sağlayan versiyon kontrol mekanizmasıdır. Her şema değişikliğini bir dosya (migration file) halinde, sıralı ve versiyonlanmış şekilde tanımlayarak çözer.Her migration genelde iki fonksiyon içerir:up() değişikliği uygular (örn. yeni sütun ekler).down(): değişikliği geri alır

**Index** bir veritabanı tablosunda veya koleksiyonunda belirli sütun(lar) üzerinden yapılan sorguların hızını artırmak için oluşturulan, ayrı bir veri yapısıdır. veritabanı motoru index yoksa full table scan (veya MongoDB'de collection scan) yapar — yani her satırı/dokümanı tek tek kontrol eder.Index, genellikle B-tree (dengeli ağaç) yapısında tutulur.

Ekstra disk alanı kaplar.Bu yüzden her sütuna index eklemek yanlıştır; sadece sık sorgulanan, filtrelenen veya sıralanan alanlara index eklenmelidir.

**Transaction** (işlem), bir veritabanında tek bir mantıksal birim olarak ele alınan, bir veya birden fazla işlemden (okuma/yazma) oluşan ve ya tamamen başarıyla tamamlanan ya da hiç gerçekleşmemiş gibi geri alınan işlem dizisidir. "Hep ya da hiç" (all-or-nothing) prensibiyle çalışır.

Birden fazla transaction aynı anda çalıştığında oluşabilecek sorunları (dirty read, non-repeatable read, phantom read) önlemek için farklı izolasyon seviyeleri vardır:
READ UNCOMMITTED (en zayıf, en hızlı)
READ COMMITTED
REPEATABLE READ
SERIALIZABLE (en güçlü,en yavas)