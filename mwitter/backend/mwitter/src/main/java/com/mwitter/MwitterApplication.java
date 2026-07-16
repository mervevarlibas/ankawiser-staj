package com.mwitter; //bu sınıf com.mwitter paketi içinde bulunuyor.

import org.springframework.boot.SpringApplication;//Spring Boot uygulamasını başlatmak için kullanılan sınıf
import org.springframework.boot.autoconfigure.SpringBootApplication;//Spring Boot uygulamasının otomatik yapılandırmasını sağlayan anotasyon

@SpringBootApplication // bu sınıfın bir Spring Boot uygulaması olduğunu belirtir. Bu anotasyon,
                       // uygulamanın başlatılması için gerekli yapılandırmaları otomatik olarak yapar.
public class MwitterApplication {

    public static void main(String[] args) {
        SpringApplication.run(MwitterApplication.class, args);
    }

}
