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
            "/member/signup",
            "/member/signup-code",
            "/member/login",
            "/member/find-username",
            "/member/send-reset-password-email",
            "/member/reset-password",

            // academy
            "/academy/login",
            "/academy/signup",
            "/academy/send-reset-password",
            "/academy/reset-password",

            // consultations
            "/consultations/request",
            "/consultations/detail",

            // swagger
            "/v3/api-docs/**",
            "/swagger-ui/**",

            "/swagger-ui.html",
            "/swagger-ui/index.html",
            "/.well-known/acme-challenge/**",
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


                        // 멤버 로그아웃: 멤버(학생/부모/교사)
                        .requestMatchers("/member/logout").hasAnyRole("STUDENT","PARENT","TEACHER")
                      // 학원 로그아웃
                        // 역할별 접근 제어

                        .requestMatchers("/academy/logout").hasRole("ACADEMY")

                        // 부모 전용: 자녀 추가 (POST /member/add-students) — 주체 일치 @PreAuthorize로 추가 검증
                        .requestMatchers(HttpMethod.POST, "/member/add-students").hasRole("PARENT")
                        // 부모 삭제 API(경로상 학생에 등록된 부모 삭제): TEACHER 또는 ACADEMY가 관리할 수 있게 제한
                        .requestMatchers(HttpMethod.DELETE, "/member/parent/**").hasAnyRole("TEACHER","ACADEMY")

                        // 교사-학생(다대다)
                        // 추가/삭제는 교사 본인 또는 학원에서만 가능 — 세부는 @PreAuthorize에서 teacherUsername 일치 검사
                        .requestMatchers(HttpMethod.POST,   "/member/teachers/*/students").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.DELETE, "/member/teachers/*/students").hasAnyRole("TEACHER","ACADEMY")
                        // 조회는 인증만 요구(권한 필터링은 서비스에서 처리)
                        .requestMatchers(HttpMethod.GET, "/member/teachers/*/students").authenticated()
                        .requestMatchers(HttpMethod.GET, "/member/students/*/teachers").authenticated()

                        // 상담
                        .requestMatchers(HttpMethod.POST, "/consultations/change-status").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.GET,  "/consultations/schedule").hasAnyRole("TEACHER","ACADEMY")

                        // 레슨
                        .requestMatchers(HttpMethod.POST,   "/lesson/create").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.PATCH,  "/lesson").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.DELETE, "/lesson/*").hasAnyRole("TEACHER","ACADEMY")
                        // 학생이 수업 등록 (현재 컨트롤러 시그니처에 맞춤)
                        .requestMatchers(HttpMethod.POST, "/lesson/*/students/*").hasRole("STUDENT")
                        // 레슨 조회는 로그인 사용자
                        .requestMatchers(HttpMethod.GET, "/lesson/**").authenticated()

                        // 출석: 교사만
                        .requestMatchers("/attendance/**").hasRole("TEACHER")

                        // 공지: 생성/수정/삭제는 교사 또는 학원, 조회는 인증 사용자
                        .requestMatchers(HttpMethod.POST,   "/announce/create").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.PATCH,  "/announce").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.DELETE, "/announce/*").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers(HttpMethod.GET,    "/announce/**").authenticated()

                        // 과제: 관리자성(교사/학원) 조회는 제한, 나머지 조회는 인증 사용자
                        .requestMatchers(HttpMethod.GET, "/task/*/members").hasAnyRole("TEACHER","ACADEMY")
                        .requestMatchers("/task/**").authenticated()
                        .requestMatchers("/member/create-username").authenticated()

                        //계정 탈퇴
                        .requestMatchers("/member/delete-user").hasAnyRole("STUDENT","TEACHER","PARENT")

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
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}