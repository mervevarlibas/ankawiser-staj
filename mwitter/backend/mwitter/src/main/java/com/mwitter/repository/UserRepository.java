package com.mwitter.repository;
//repository sınıfı, veritabanı işlemlerini gerçekleştirmek için kullanılan bir arayüzdür. Bu sınıf, User modelini kullanarak MongoDB veritabanında kullanıcı verilerini yönetir.
import java.util.List;
import java.util.Optional;//arayüzün döndüreceği değerin null olabileceğini belirtmek için kullanılır(Boş Değer Hatası) almamak için kullanılan harika bir güvenlik önlemidir
import org.springframework.data.mongodb.repository.MongoRepository;//Repository gidip MongoDB'den veriyi alıyor.Veritabanı sorguları yazmaktan kurtuluyoruz.
import com.mwitter.model.User;//user sınıfını kullanabilmek için import ediyoruz

public interface UserRepository extends MongoRepository<User, String> {//user modelimin idsi string tipinde.bunu mongodbye bağla. bunun sayesinde otomatik save,delete,findby gibi komutları kazanıyor.

    Optional<User> findByEmail(String email);//User sınıfındaki email alanına göre arama yap.kayıt olma işleminde kulllanıyoruz

    Optional<User> findByUsername(String username);//giriş yapma işleminde kullanıyoruz

    List<User> findByUsernameContainingIgnoreCase(String username);//kullanıcı adının içinde geçenleri liste olarak getir.

}
