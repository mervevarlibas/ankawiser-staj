package com.mwitter.config;

import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;//application.properties/ortam değişkenlerinden değer enjekte etmek için (@Value).
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;//Şifreleri hash'lemek için
import org.springframework.stereotype.Component;

import com.mwitter.model.Role;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

@Component
public class AdminAccountInitializer implements ApplicationRunner {//Spring bean'i olarak kaydedilir ve ApplicationRunner implemente ettiği için uygulama başladıktan sonra otomatik çalışır.

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);//Bu sınıfa özel logger nesnesi.

    private final UserRepository userRepository;//Spring, ilk iki parametreyi (repository, encoder) normal dependency injection ile sağlar.
    private final BCryptPasswordEncoder passwordEncoder;
    private final String adminEmail;//Son üç parametre ise @Value ile properties/environment'tan okunur:
    private final String adminPassword;
    private final String adminUsername;

    public AdminAccountInitializer(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            @Value("${admin.email:}") String adminEmail,//property'si yoksa boş string ("") varsayılan değer olur.
            @Value("${admin.password:}") String adminPassword,
            @Value("${admin.username:mwitter-admin}") String adminUsername) {//yoksa varsayılan "mwitter-admin" kullanılır.
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
        this.adminUsername = adminUsername.trim();
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() && adminPassword.isBlank()) {
            log.warn("Yönetici hesabı oluşturulmadı. ADMIN_EMAIL ve ADMIN_PASSWORD ayarlanmadı.");
            return;
        }
        if (adminEmail.isBlank() || adminPassword.length() < 12 || adminUsername.isBlank()) {
            throw new IllegalStateException(
                    "ADMIN_EMAIL, ADMIN_USERNAME ve en az 12 karakterli ADMIN_PASSWORD ayarlanmalıdır.");
        }

        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);//Verilen email'e sahip kullanıcı var mı diye veritabanında bakılır. Varsa o kullanıcı alınır, yoksa yeni boş bir User nesnesi oluşturulur
        boolean newAccount = admin.getId() == null;
        userRepository.findByUsername(adminUsername)//verilen username'i kullanan başka bir kullanıcı var mı diye bakılır.
                .filter(owner -> !Objects.equals(owner.getId(), admin.getId()))//eğer böyle bir kullanıcı bulunduysa yani gerçekten başka bir hesapsa filtreden geçer
                .ifPresent(owner -> {//eğer gerçekten başka bir hesap bu username'i kullanıyorsa, exception fırlatılır ve işlem durdurulur.
                    throw new IllegalStateException("ADMIN_USERNAME başka bir hesap tarafından kullanılıyor.");
                });
        admin.setEmail(adminEmail);
        admin.setUsername(adminUsername);//Email ve username bilgileri (yeni ya da mevcut) admin nesnesine yazılır.
        if (newAccount || !passwordEncoder.matches(adminPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
        }
        if (newAccount) {
            admin.setRegistrationDate(LocalDateTime.now());
        }
        admin.setRole(Role.ADMIN);
        admin.setVerified(true);
        admin.setVerificationCode(null);
        admin.setVerificationCodeExpiry(null);
        userRepository.save(admin);

        log.info(newAccount ? "Yönetici hesabı oluşturuldu." : "Yönetici hesabı ve yetkisi güncellendi.");
    }
}
