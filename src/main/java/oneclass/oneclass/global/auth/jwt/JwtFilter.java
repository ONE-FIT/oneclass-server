package oneclass.oneclass.global.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtFilter.class);
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

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
            String jwt = isLikelyJwe(token) ? jwtProvider.decryptToken(token) : token;

            jwtProvider.validateToken(jwt); // 실패 시 예외 발생

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtProvider.getAllClaims(jwt);
                String username = claims.getSubject();
                Collection<? extends GrantedAuthority> authorities = toAuthorities(claims);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            log.debug("JWT invalid or parsing failed: {}", e.getMessage());
            // 필요 시 즉시 401 반환
            // sendUnauthorized(response, "Invalid or expired token");
            // return;
        }

        chain.doFilter(request, response);
    }

    private String extractBearer(HttpServletRequest request) {
        String b = request.getHeader("Authorization");
        if (b != null && b.startsWith("Bearer ")) return b.substring(7);
        return null;
    }

    private boolean isLikelyJwe(String t) {
        if (t == null) return false;
        int dot = 0;
        for (int i = 0; i < t.length(); i++) if (t.charAt(i) == '.') dot++;
        return dot == 4;
    }

    private Collection<? extends GrantedAuthority> toAuthorities(Claims claims) {
        List<GrantedAuthority> list = new ArrayList<>();

        // 1) role 단일 (문자열)
        Object roleObj = claims.get("role");
        if (roleObj instanceof String rs) {
            list.add(new SimpleGrantedAuthority(normalizeRole(rs)));
        }

        // 2) roles 배열 (["ADMIN","TEACHER"] or ["ROLE_ADMIN"])
        Object rolesObj = claims.get("roles");
        if (rolesObj instanceof Collection<?> col) {
            col.forEach(o -> {
                if (o != null) list.add(new SimpleGrantedAuthority(normalizeRole(o.toString())));
            });
        } else if (rolesObj instanceof String csv) {
            // roles: "ADMIN,TEACHER"
            Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(r -> list.add(new SimpleGrantedAuthority(normalizeRole(r))));
        }

        if (list.isEmpty()) {
            list.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        // 중복 제거
        return list.stream().distinct().collect(Collectors.toList());
    }

    private String normalizeRole(String raw) {
        if (raw == null || raw.isBlank()) return "ROLE_USER";
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }

    @SuppressWarnings("unused")
    private void sendUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\":\"UNAUTHORIZED\",\"message\":\"" + msg + "\"}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}