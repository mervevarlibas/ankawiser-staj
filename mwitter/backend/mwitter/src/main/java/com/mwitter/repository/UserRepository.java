package com.mwitter.repository;
//repository sınıfı, veritabanı işlemlerini gerçekleştirmek için kullanılan bir arayüzdür. Bu sınıf, User modelini kullanarak MongoDB veritabanında kullanıcı verilerini yönetir.

import java.util.List;
import java.util.Optional;//arayüzün döndüreceği değerin null olabileceğini belirtmek için kullanılır

import org.springframework.data.mongodb.repository.MongoRepository;//Repository gidip MongoDB'den veriyi alıyor.

import com.mwitter.model.User;//user sınıfını kullanabilmek için import ediyoruz

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);//User sınıfındaki email alanına göre arama yap

    Optional<User> findByUsername(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

}
