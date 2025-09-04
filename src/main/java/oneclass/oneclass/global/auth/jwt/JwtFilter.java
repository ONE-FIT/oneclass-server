package oneclass.oneclass.global.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtProvider jwtProvider;

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // permitAll 경로는 여기서 바로 통과
    private boolean isPermitAllPath(String path) {
        return path.startsWith("/api/member/")// 로그인/회원가입
                || path.startsWith("/api/consultations/request")
                || path.startsWith("/api/consultations/detail")
                || path.startsWith("/swagger-ui/")    // 스웨거 UI
                || path.startsWith("/v3/api-docs")    // 스웨거 문서
                || path.startsWith("/error");         // 에러 엔드포인트
    }

    // JWE compact serialization 은 점(.) 이 4개라 5개 조각
    private boolean looksLikeJwe(String token) {
        if (token == null) return false;
        int dots = 0;
        for (int i = 0; i < token.length(); i++) {
            if (token.charAt(i) == '.') dots++;
        }
        return dots == 4;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        log.info("요청 경로: " + path);

        // CORS preflight 또는 permitAll 경로는 바로 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isPermitAllPath(path)) {
            log.info("permitAll 경로로 필터 통과: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        // 토큰 없으면 그냥 통과 (인증 필요 자원은 Security에서 401 처리함)
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // JWE면 복호화, 아니면 평문 JWT 그대로 사용
            String plainJwt = looksLikeJwe(token) ? jwtProvider.decyptToken(token) : token;

            // 검증 실패 시 컨텍스트 설정하지 않고 통과
            if (!jwtProvider.validateToken(plainJwt)) {
                log.debug("Invalid JWT");
                filterChain.doFilter(request, response);
                return;
            }

            // 이미 인증된 경우가 아니면 세팅
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtProvider.getUsername(plainJwt);

                // TODO: 권한은 추후 실제 값으로 교체
                String role = jwtProvider.getRole(plainJwt);
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // 복호화/파싱 등 실패 – 여기서 에러 응답을 보내지 않음
            // (AuthenticationEntryPoint/AccessDeniedHandler가 최종 응답)
            log.debug("JWT processing failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

}