package oneclass.oneclass.global.config;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.jwt.JwtFilter;
import oneclass.oneclass.global.auth.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    private static final String[] PUBLIC_ENDPOINTS = {
            // member
            "/member/**",
//            "/member/signup-code",
//            "/member/login",
//            "/member/find-username",
//            "/member/send-reset-password-email",
//            "/member/reset-password",

            // academy
            "/academy/**",
//            "/academy/signup",
//            "/academy/send-reset-password",
//            "/academy/reset-password",

            // consultations
            "/consultations/**",
//            "/consultations/detail",

            "/attendance/**",
            "/announcements/**",
            "/lessons/**",
            "/tasks/**",
            "/lms/**",

            // swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",

            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/.well-known/acme-challenge/**",
//            "/member/admin/signup",

    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()


//
                        // 그 외 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 CORS 설정 적용
        return new CorsFilter(source);
    }

}