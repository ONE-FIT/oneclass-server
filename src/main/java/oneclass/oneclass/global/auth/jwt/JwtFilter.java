package oneclass.oneclass.global.auth.jwt;

import io.jsonwebtoken.Claims;
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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import java.util.*;

import java.util.stream.Collectors;

/**
 * JwtFilter
 * - Authorization: Bearer <token> 을 읽어 인증 컨텍스트를 채웁니다.
 * - JWE(5 세그먼트) 토큰이면 JwtProvider.decryptToken(...)으로 복호화 후 검증합니다.
 * - role/roles 클레임을 기반으로 GrantedAuthority를 구성합니다.
 * 주의:
 * - principal은 username(String)으로 설정합니다.
 *   컨트롤러에서는 Authentication.getName() 으로 username을 읽어 쓰세요.
 *   (만약 @AuthenticationPrincipal CustomUserDetails 가 필요하면 아래 주석 참조)
 */
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        // Preflight
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
            // JWE(암호화) 토큰이면 먼저 복호화해서 JWS로 변환
            String candidate = isLikelyJwe(token) ? jwtProvider.decryptToken(token) : token;

            // 유효성 검증 (만료/서명 등)
            jwtProvider.validateToken(candidate);

            // 이미 인증돼 있지 않다면 컨텍스트 설정
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtProvider.getAllClaims(candidate);
                String username = claims.getSubject();
                Collection<? extends GrantedAuthority> authorities = toAuthorities(claims);

                // 기본: principal을 username(String)으로 설정
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);


                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            // 토큰이 잘못되었거나 만료된 경우: 컨텍스트를 건드리지 않고 다음 필터로 넘김
            log.debug("JWT invalid or parsing failed: {}", e.getMessage());
        }

        chain.doFilter(request, response);
    }

    private String extractBearer(HttpServletRequest request) {
        String b = request.getHeader("Authorization");
        return (b != null && b.startsWith("Bearer ")) ? b.substring(7).trim() : null;
    }

    // JWE compact serialization은 5개의 세그먼트를 가짐
    private boolean isLikelyJwe(String t) {
        if (t == null) return false;
        int dot = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dot++;
        return dot == 4;
    }

    private Collection<? extends GrantedAuthority> toAuthorities(Claims claims) {
        List<GrantedAuthority> list = new ArrayList<>();

        // 1) 단일 역할: "role": "ADMIN" | "ROLE_ADMIN"
        Object roleObj = claims.get("role");
        if (roleObj instanceof String rs && !rs.isBlank()) {
            list.add(new SimpleGrantedAuthority(normalizeRole(rs)));
        }

        // 2) 다중 역할:
        //    "roles": ["ADMIN","TEACHER"] or ["ROLE_ADMIN"]
        //    "roles": "ADMIN,TEACHER"
        Object rolesObj = claims.get("roles");
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

        if (list.isEmpty()) {
            // 정책상 기본 권한
            list.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // 중복 제거
        return list.stream().distinct().collect(Collectors.toList());
    }

    private String normalizeRole(String raw) {
        if (raw == null || raw.isBlank()) return "ROLE_USER";
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }
}