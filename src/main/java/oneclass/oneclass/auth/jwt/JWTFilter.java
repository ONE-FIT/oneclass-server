package oneclass.oneclass.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTProvider jwtProvider;

    public JWTFilter(JWTProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = resolveToken(request);

        if (bearerToken != null) {
            try {
                // 1. JWE 복호화
                String plainToken = jwtProvider.decyptToken(bearerToken);

                // 2. JWT 검증
                if (jwtProvider.validateToken(plainToken)) {
                    // 3. username 추출
                    String username = jwtProvider.getUsername(plainToken);

                    // 4. 권한 추출
                    // JWTProvider에 getRole(String token) 메서드가 있다고 가정
                    String role = jwtProvider.getRole(plainToken);
                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                    // 5. Authentication 객체 생성
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    // 6. SecurityContext에 저장
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
                    return;
                }
            } catch (Exception e) {
                // logger.error("JWT 인증 실패", e); // logger 선언 필요 혹은 System.err.println 사용
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}