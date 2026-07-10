package com.mwitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mwitter.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session
                        -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS//sunucuda klasik oturum saklanmaz.her istek kendi jwtsi ile kimliğini kanıtlar
                )
                )
                .authorizeHttpRequests(auth -> auth
                .requestMatchers(//token olmadan kullanılabilir çünkü kullanıcı o işlemleri yaparken token a sahip değil
                        "/users/register",
                        "/users/login"
                ).permitAll()
                .requestMatchers(//gönderileri ve profilleri görüntüleme şimdilik!!!!! herkese açık
                        HttpMethod.GET,
                        "/posts/**",
                        "/users/**"
                ).permitAll()
                .anyRequest().authenticated()//bunlar dışındaki istekler geçerli jwt taşınmasını ister
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
