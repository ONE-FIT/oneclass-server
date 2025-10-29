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
import java.util.*;
import java.util.stream.Collectors;

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
            String candidate = isLikelyJwe(token) ? jwtProvider.decryptToken(token) : token;

            jwtProvider.validateToken(candidate);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                Claims claims = jwtProvider.getAllClaims(candidate);

                // subject는 phone
                String phone = claims.getSubject();

                // 선택적 username/name 클레임
                String username = null;
                Object uo = claims.get(JwtProvider.USERNAME_CLAIM_KEY);
                if (uo != null) {
                    String s = uo.toString();
                    if (!s.isBlank()) username = s;
                }
                String name = null;
                Object no = claims.get(JwtProvider.NAME_CLAIM_KEY);
                if (no != null) {
                    String s = no.toString();
                    if (!s.isBlank()) name = s;
                }

                // principal: username이 있으면 username, 없으면 phone
                String principalName = (username != null && !username.isBlank()) ? username : phone;

                Collection<? extends GrantedAuthority> authorities = toAuthorities(claims);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(principalName, null, authorities);

                // 필요하다면 phone/username/name을 리퀘스트 어트리뷰트로 내려서 컨트롤러/서비스에서 사용 가능하게 함
                request.setAttribute("auth.phone", phone);
                if (username != null) request.setAttribute("auth.username", username);
                if (name != null) request.setAttribute("auth.name", name);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            log.debug("JWT invalid or parsing failed: {}", e.getMessage());
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