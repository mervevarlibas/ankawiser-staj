package com.mwitter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import java.util.List;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.mwitter.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean//springe der ki bu metodun döndürdüğü nesneyi proje boyunca kullan yanş securityfilterchain
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                 .cors(cors ->//5500 portundan gelen isteklere izin ver.
                cors.configurationSource(corsConfigurationSource())
        )
        .csrf(csrf -> csrf.disable())
                .sessionManagement(session
                        -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS//sunucuda klasik oturum saklanmaz.her istek kendi jwtsi ile kimliğini kanıtlar
                )
                )
                .authorizeHttpRequests(auth -> auth//hangi endpointlere kim girebilir
                .requestMatchers(//token olmadan kullanılabilir çünkü kullanıcı o işlemleri yaparken token a sahip değil
                        "/users/register",
                        "/users/login", //token isteme
                        "/users/verify-email",
                        "/users/resend-verification-code"
                ).permitAll()
                .requestMatchers(//gönderileri ve profilleri görüntüleme şimdilik!!!!! herkese açık
                        HttpMethod.GET,
                        "/posts/**",
                        "/users/**"
                ).permitAll()
                .anyRequest().authenticated()//bunlar dışındaki istekler geçerli jwt taşınmasını ister
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(//bu filtre controllerdan önce çalışır. görevi headerdaki authorization bilgisni okumak
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
@Bean
public CorsConfigurationSource corsConfigurationSource() {

    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of(//Hangi frontend adreslerinin backend’e istek atmasına izin verildiğini belirtir
            "http://127.0.0.1:5500",
            "http://localhost:5500"
    ));

    configuration.setAllowedMethods(List.of(//izin verilen http metodları
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
    ));

    configuration.setAllowedHeaders(List.of(//ağağıdaki başlıkları göndermesine izin verir
            "Authorization",
            "Content-Type"
    ));

    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);//Bu ayarları bütün backend endpointlerine uygular.

    return source;
}

}
