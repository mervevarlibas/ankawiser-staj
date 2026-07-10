package com.mwitter.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration//bu sınıf uygulama ayarları ve bean tanımları içeriyor.
public class PasswordConfig {
@Bean//bu metodun döndürdüğü nesneyi oluştur sakla ihtiyaç duyan sınıflara ver
    public BCryptPasswordEncoder passwordEncoder() {//bu metod bir byrcryptPasswordEncoder nesnesi döndürüyor. Bu nesne şifreleri güvenli bir şekilde saklamak için kullanılır.

        return new BCryptPasswordEncoder();//nesneyi spring yönetir
    }
}
