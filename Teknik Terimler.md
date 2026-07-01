# Git & Araçlar
**Git** dosya değişikliklerini zaman içinde takip eden bir versiyon kontrol sistemidir .Her geliştirici, tüm repository geçmişini (tüm commit'ler, branch'ler) lokal bilgisayarında tam kopyasıyla taşır. Sunucu olmadan da commit atabilir, geçmişe bakabilir, branch açabilirsin.

**Repository** repo, bir projeye ait tüm dosyaların, bu dosyaların değişim geçmişinin (commit history) ve Git meta verisinin bir arada saklandığı merkezi veri deposudur. Sadece "proje klasörü" değil — projenin tüm zamansal geçmişini, branch'lerini, tag'lerini ve konfigürasyonunu içeren tam bir veritabanıdır.

Local repository: Kendi bilgisayarındaki kopya. git init ile sıfırdan oluşturulur veya git clone ile uzaktan indirilir. Tüm geçmiş burada tam olarak bulunur — internet bağlantısı olmadan da commit atabilir, branch açabilir, geçmişe bakabilirsin.
Remote repository: GitHub, GitLab, Bitbucket gibi platformlarda barındırılan uzak kopya. Ekip üyeleri arasında kod paylaşımının ve senkronizasyonun merkezi. git push ile local'den remote'a, git pull ile remote'dan local'e değişiklikler aktarılır.

**Branch** (dal), Git'te belirli bir commit'e işaret eden, bağımsız bir geliştirme hattı oluşturmak için kullanılan hafif bir pointer (işaretçi)'dır.
Ana kod tabanını (genelde main/master) bozmadan, bağımsız bir ortamda yeni özellik geliştirmek, bug fix yapmak veya deney yapmak için. Her branch kendi izole çalışma alanıdır — bir branch'teki değişiklikler, merge edilene kadar diğer branch'leri etkilemez

Git'in iç yapısında branch, .git/refs/heads/ altında branch adıyla saklanan ve o branch'in son commit'inin hash'ini tutan bir dosyadır.her yeni commit atıldığında, o branch'in dosyasındaki hash otomatik güncellenir — branch pointer ilerler.

Feature Branch Workflow (en yaygın):
Her yeni özellik için ayrı branch açılır, tamamlanınca main'e merge edilir.

**Pull Request** bir branch'teki değişikliklerin başka bir branch'e (genelde main) merge edilmesi için açılan, kod incelemesi (code review), tartışma ve onay sürecini yöneten işbirliği mekanizmasıdır. 
main'den yeni bir branch oluşturursun.
Örneğin: login-feature
Bu branch'te kodlarını yazıp GitHub'a gönderirsin (push).
Ardından Pull Request (PR) açarsın.
Ekip arkadaşların kodunu inceler (code review yapar).
Gerekirse düzeltme istenir.
Testler başarılıysa PR onaylanır ve merge edilerek main branch'ine eklenir.

Unit ve Integration Testleri otomatik çalıştırılabilir

Pull Request: "Ben kodumu bitirdim, inceleyip ana projeye ekleyebilir misiniz?" isteğidir.
Merge: Bu isteğin onaylanıp kodun gerçekten ana branch'e eklenmesidir.

**Code Review** (Kod İncelemesi): Bir geliştiricinin yazdığı kodun, başka bir geliştirici tarafından incelenmesi ve değerlendirilmesi sürecidir.

**Merge Conflict** (Birleştirme Çakışması): İki farklı kişi veya iki farklı branch, aynı dosyanın aynı kısmını farklı şekilde değiştirdiğinde, Git'in hangi değişikliği kullanacağına karar verememesi durumudur.
Geliştirici:
Çakışan dosyayı açar.
Hangi kodun kalacağına karar verir (veya iki kodu birleştirir).
Dosyayı kaydeder.
Değişiklikleri commit edip merge işlemini tamamlar.

Merge Conflict, iki farklı değişiklik aynı kod üzerinde çakıştığında Git'in hangisini kullanacağını bilememesi durumudur. Geliştirici çakışmayı manuel olarak çözer.