package com.mwitter.config;

import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.mwitter.model.Role;
import com.mwitter.model.User;
import com.mwitter.repository.UserRepository;

@Component
public class AdminAccountInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminUsername;

    public AdminAccountInitializer(
            UserRepository userRepository,
            BCryptPasswordEncoder passwordEncoder,
            @Value("${admin.email:}") String adminEmail,
            @Value("${admin.password:}") String adminPassword,
            @Value("${admin.username:mwitter-admin}") String adminUsername) {
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

        User admin = userRepository.findByEmail(adminEmail).orElseGet(User::new);
        boolean newAccount = admin.getId() == null;
        userRepository.findByUsername(adminUsername)
                .filter(owner -> !Objects.equals(owner.getId(), admin.getId()))
                .ifPresent(owner -> {
                    throw new IllegalStateException("ADMIN_USERNAME başka bir hesap tarafından kullanılıyor.");
                });
        admin.setEmail(adminEmail);
        admin.setUsername(adminUsername);
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
