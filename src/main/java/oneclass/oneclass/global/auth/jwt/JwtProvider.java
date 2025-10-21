package oneclass.oneclass.global.auth.jwt;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import oneclass.oneclass.domain.member.dto.ResponseToken;
import oneclass.oneclass.domain.member.error.TokenError;
import oneclass.oneclass.global.exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    public static final String ROLE_CLAIM_KEY = "role";
    public static final String ROLES_CLAIM_KEY = "roles";

    private final String secret;       // JWS(HMAC)용
    private final String jweSecret;    // JWE(AES-*-GCM)용(선택)
    private final long accessValidityMillis;
    private final long refreshValidityMillis;
    private Key key;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long accessValiditySeconds,
            @Value("${jwt.refresh-validity-in-seconds:0}") long refreshValiditySeconds,
            @Value("${jwt.jwe-secret:}") String jweSecret   // 추가: JWE 복호화용 키(없으면 secret 사용)
    ) {
        this.secret = secret;
        this.jweSecret = jweSecret;
        this.accessValidityMillis = accessValiditySeconds * 1000;
        this.refreshValidityMillis =
                (refreshValiditySeconds > 0 ? refreshValiditySeconds * 1000 : accessValiditySeconds * 2000);
    }

    @PostConstruct
    public void init() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret length must be >= 32 chars for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // ===== 발급 =====
    public ResponseToken generateToken(String subject, String roleValue) {
        String access = generateAccessToken(subject, roleValue);
        String refresh = generateRefreshToken(subject);
        return new ResponseToken(access, refresh);
    }

    public String generateAccessToken(String subject, String roleValue) {
        long exp = System.currentTimeMillis() + accessValidityMillis;
        return buildJwt(subject, roleValue, exp, true);
    }

    private String generateRefreshToken(String subject) {
        long exp = System.currentTimeMillis() + refreshValidityMillis;
        return buildJwt(subject, null, exp, false);
    }

    private String buildJwt(String subject, String roleValue, long expiryEpochMillis, boolean includeRole) {
        Date now = new Date();
        Date exp = new Date(expiryEpochMillis);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp);

        if (includeRole && roleValue != null) {
            builder.claim(ROLE_CLAIM_KEY, roleValue);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // ===== 검증/클레임 =====
    public boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalid: {}", e.getMessage());
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    public Claims getAllClaims(String token) {
        try {
            return parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired while reading claims: {}", e.getMessage());
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT invalid while reading claims: {}", e.getMessage());
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    public String getUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    private Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    // ===== JWE 복호화(복구) =====
    // Compact JWE(5 세그먼트) → 평문 문자열 반환.
    // 일반적으로 Nested JWT를 쓴다면 평문은 JWS(3 세그먼트)여서 그 다음 validateToken(...)으로 검증.
    public String decryptToken(String jweToken) {
        try {
            String useKey = (jweSecret != null && !jweSecret.isBlank()) ? jweSecret : secret;
            byte[] aesKeyBytes = useKey.getBytes(StandardCharsets.UTF_8);

            // Direct 암호화("alg":"dir")를 가정(키 길이는 enc에 따라 16/24/32 바이트 권장)
            JWEObject jweObject = JWEObject.parse(jweToken);
            var decrypter = new DirectDecrypter(aesKeyBytes);
            jweObject.decrypt(decrypter);

            return jweObject.getPayload().toString();
        } catch (Exception e) {
            log.debug("JWE decrypt failed: {}", e.getMessage());
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    // ===== 편의 =====
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public List<String> extractRoleStrings(String token) {
        Claims claims = getAllClaims(token);
        return extractRoleStrings(claims);
    }

    public List<String> extractRoleStrings(Claims claims) {
        Set<String> roleSet = new LinkedHashSet<>();
        Object single = claims.get(ROLE_CLAIM_KEY);
        if (single instanceof String s) roleSet.add(normalizeRole(s));

        Object multi = claims.get(ROLES_CLAIM_KEY);
        if (multi instanceof Collection<?> col) {
            for (Object o : col) if (o != null) roleSet.add(normalizeRole(o.toString()));
        } else if (multi instanceof String csv) {
            Arrays.stream(csv.split(",")).map(String::trim).filter(v -> !v.isEmpty())
                    .forEach(v -> roleSet.add(normalizeRole(v)));
        }

        if (roleSet.isEmpty()) roleSet.add("ROLE_USER");
        return new ArrayList<>(roleSet);
    }

    private String normalizeRole(String raw) {
        if (raw == null || raw.isBlank()) return "ROLE_USER";
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }

    public record TokenPair(String accessToken, String refreshToken, long accessTokenExpiresAt, long refreshTokenExpiresAt) {}
}