package com.mwitter.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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
        .csrf(csrf -> csrf.disable())//CSRF (Siteler Arası İstek Sahtekarlığı), eski tip (Session/Cookie kullanan) web siteleri için bir kalkanıdır. Biz JWT (Token) kullandığımız için bu eski kalkanı kapatıyoruz; aksi takdirde frontend'den gelen tüm POST istekleri reddedilirdi.
                .sessionManagement(session
                        -> session.sessionCreationPolicy(//Sunucu kimseyi hatırlamaz
                        SessionCreationPolicy.STATELESS//sunucuda klasik oturum saklanmaz.her istek kendi jwtsi ile kimliğini kanıtlar
                )
                )
                .authorizeHttpRequests(auth -> auth//hangi endpointlere kim girebilir
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/**").denyAll()
                .requestMatchers(
                        "/", "/*.html", "/css/**", "/js/**", "/images/**",
                        "/icons/**", "/manifest.webmanifest", "/sw.js"
                ).permitAll()
                .requestMatchers(//token olmadan kullanılabilir çünkü kullanıcı o işlemleri yaparken token a sahip değil
                        "/users/register",
                        "/users/login", //token isteme
                        "/users/verify-email",
                        "/users/resend-verification-code"
                        , "/users/forgot-password"//Şifresini unutan kullanıcıda JWT bulunamayacağı için reset maili istemeyi girişsiz erişime açar.
                        , "/users/reset-password"//Mail linkinden gelen kullanıcının JWT'si olmadığı için yeni şifre gönderme endpoint'ini girişsiz erişime açar.
                        , "/ws/**"
                ).permitAll()
                .requestMatchers(//gönderileri ve profilleri görüntüleme şimdilik!!!!! herkese açık
                        HttpMethod.GET,//Sadece okuma (GET) işlemlerine izin verilmiş.
                        "/posts/**",
                        "/users/**"
                ).permitAll()
                .anyRequest().authenticated()//bunlar dışındaki istekler geçerli jwt taşınmasını ister
                )
                .httpBasic(httpBasic -> httpBasic.disable())
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

    configuration.setAllowedHeaders(List.of(//ağağıdaki başlıkları göndermesine izin verir.frontendin bize token de json gönderebilmesi için izin verir
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
