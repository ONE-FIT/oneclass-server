package oneclass.oneclass.global.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // CORS 사전 요청(OPTIONS)은 토큰 검증 없이 통과
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String token = extractBearer(request);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 1. 토큰 복호화 및 검증
            String candidate = isLikelyJwe(token) ? jwtProvider.decryptToken(token) : token;
            jwtProvider.validateToken(candidate);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtProvider.getAllClaims(candidate);

                // 2. 정보 추출
                String phone = claims.getSubject();
                String username = null;
                Object uo = claims.get(JwtProvider.USERNAME_CLAIM_KEY);
                if (uo != null) username = uo.toString();

                // 3. 권한 추출
                Collection<? extends GrantedAuthority> authorities = toAuthorities(claims);

                // 4. CustomUserDetails 생성 (Principal에 설정할 객체)
                Long memberId = claims.get("id", Long.class);
                if (memberId == null) memberId = 0L;

                CustomUserDetails userDetails = CustomUserDetails.forMember(
                        memberId,
                        (username != null && !username.isBlank()) ? username : phone,
                        "",
                        (List<GrantedAuthority>) authorities
                );

                // 5. 인증 토큰 생성 및 컨텍스트 저장
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);

                // 성공 로그: 어떤 URI에 누가 접근했는지 출력
                log.info("JWT Auth Success: [{} {}] User: {}, Roles: {}",
                        request.getMethod(), request.getRequestURI(), userDetails.getUsername(), authorities);

                request.setAttribute("auth.phone", phone);
                if (username != null) request.setAttribute("auth.username", username);
            }

        } catch (Exception e) {
            // 🚨 핵심 수정: 에러 발생 시 메서드와 URI를 함께 로그로 남김
            log.error("JWT Authentication Failed for [{} {}]: {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    e.getMessage());

            // 인증 실패 시 컨텍스트를 비워 보안 유지
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    private String extractBearer(HttpServletRequest request) {
        String b = request.getHeader("Authorization");
        return (b != null && b.startsWith("Bearer ")) ? b.substring(7).trim() : null;
    }

    private boolean isLikelyJwe(String t) {
        if (t == null) return false;
        int dot = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dot++;
        return dot == 4;
    }

    private Collection<? extends GrantedAuthority> toAuthorities(Claims claims) {
        List<GrantedAuthority> list = new ArrayList<>();

        Object roleObj = claims.get(JwtProvider.ROLE_CLAIM_KEY);
        if (roleObj instanceof String rs && !rs.isBlank()) {
            list.add(new SimpleGrantedAuthority(normalizeRole(rs)));
        }

        Object rolesObj = claims.get(JwtProvider.ROLES_CLAIM_KEY);
        if (rolesObj instanceof Collection<?> col) {
            for (Object o : col) {
                if (o != null) list.add(new SimpleGrantedAuthority(normalizeRole(o.toString())));
            }
        } else if (rolesObj instanceof String csv) {
            Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(r -> list.add(new SimpleGrantedAuthority(normalizeRole(r))));
        }

        if (list.isEmpty()) list.add(new SimpleGrantedAuthority("ROLE_USER"));
        return list.stream().distinct().collect(Collectors.toList());
    }

    private String normalizeRole(String raw) {
        if (raw == null || raw.isBlank()) return "ROLE_USER";
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }
}