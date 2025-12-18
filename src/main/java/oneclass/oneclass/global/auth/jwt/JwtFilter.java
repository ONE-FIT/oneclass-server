package oneclass.oneclass.global.auth.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import oneclass.oneclass.global.auth.CustomUserDetails; // ⬅️ 추가된 임포트
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

                // 1. 정보 추출
                String phone = claims.getSubject();
                String username = null;
                Object uo = claims.get(JwtProvider.USERNAME_CLAIM_KEY);
                if (uo != null) username = uo.toString();

                // 2. 권한 추출 (ADMIN, STUDENT 등)
                Collection<? extends GrantedAuthority> authorities = toAuthorities(claims);

                // 3. ⭐️ 핵심: CustomUserDetails 객체 생성
                // principal 자리에 String이 아닌 'CustomUserDetails' 객체를 넣어야 합니다.
                // 만약 claims에 'id'가 없다면 일단 0L 등을 넣고, 토큰 발급 시 id를 포함하도록 수정해야 합니다.
                Long memberId = claims.get("id", Long.class);
                if (memberId == null) memberId = 0L; // 임시 방편

                CustomUserDetails userDetails = CustomUserDetails.forMember(
                        memberId,
                        (username != null && !username.isBlank()) ? username : phone,
                        "", // 비밀번호는 필요 없음
                        (List<GrantedAuthority>) authorities
                );

                // 4. 인증 토큰 생성 (Principal에 userDetails 객체 전달)
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                // 로그로 현재 들어온 권한 확인 (Access Denied 디버깅용)
                log.info("Authenticated User: {}, Roles: {}", userDetails.getUsername(), authorities);

                request.setAttribute("auth.phone", phone);
                if (username != null) request.setAttribute("auth.username", username);

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
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