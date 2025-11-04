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

    // common claim keys
    public static final String ROLE_CLAIM_KEY = "role";
    public static final String ROLES_CLAIM_KEY = "roles";
    public static final String PHONE_CLAIM_KEY = "phone";
    public static final String USERNAME_CLAIM_KEY = "username";
    public static final String NAME_CLAIM_KEY = "name";

    private final String secret;       // JWS(HMAC)용
    private final String jweSecret;    // JWE(AES-*-GCM)용(선택)
    private final long accessValidityMillis;
    private final long refreshValidityMillis;
    private final String issuer;
    private Key key;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long accessValiditySeconds,
            @Value("${jwt.refresh-validity-in-seconds:0}") long refreshValiditySeconds,
            @Value("${jwt.jwe-secret:}") String jweSecret,
            @Value("${jwt.issuer:oneclass}") String issuer
    ) {
        this.secret = secret;
        this.jweSecret = jweSecret;
        this.issuer = issuer;
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

    // ===== 발급 (subject는 호출부에서 결정) =====
    public ResponseToken generateToken(String subject, String roleValue) {
        String access = generateAccessToken(subject, roleValue);
        String refresh = generateRefreshToken(subject);
        return new ResponseToken(access, refresh);
    }

    public String generateAccessToken(String subject, String roleValue) {
        long exp = System.currentTimeMillis() + accessValidityMillis;
        return buildJwt(subject, Map.of(), roleValue, exp);
    }

    private String generateRefreshToken(String subject) {
        long exp = System.currentTimeMillis() + refreshValidityMillis;
        return buildJwt(subject, Map.of(), null, exp);
    }

    // ===== 전화번호 로그인 전용(권장) =====
    // access: phone/username/name(+role) 포함
    // refresh: 표준 클레임만(sub/iss/iat/exp/jti). 부가 클레임 미포함 → 토큰 길이 최소화
    public ResponseToken generateTokenByPhone(String phone, String roleValue, String usernameOrNull, String nameOrNull) {
        long now = System.currentTimeMillis();
        String access = buildJwtByPhone(phone, roleValue, usernameOrNull, nameOrNull, now + accessValidityMillis, true);
        String refresh = buildJwtByPhone(phone, null, usernameOrNull, nameOrNull, now + refreshValidityMillis, false);
        return new ResponseToken(access, refresh);
    }

    public String generateAccessTokenByPhone(String phone, String roleValue, String usernameOrNull, String nameOrNull) {
        return buildJwtByPhone(phone, roleValue, usernameOrNull, nameOrNull,
                System.currentTimeMillis() + accessValidityMillis, true);
    }

    public String generateRefreshTokenByPhone(String phone, String usernameOrNull, String nameOrNull) {
        return buildJwtByPhone(phone, null, usernameOrNull, nameOrNull,
                System.currentTimeMillis() + refreshValidityMillis, false);
    }

    // 공통 빌더
    private String buildJwt(String subject, Map<String, Object> extraClaims, String roleValue, long expiryEpochMillis) {
        Date now = new Date();
        Date exp = new Date(expiryEpochMillis);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(exp)
                .setId(UUID.randomUUID().toString());

        if (roleValue != null) {
            builder.claim(ROLE_CLAIM_KEY, roleValue);
        }
        if (extraClaims != null && !extraClaims.isEmpty()) {
            builder.addClaims(extraClaims);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // access=true → 부가 클레임 포함, access=false(refresh) → 부가 클레임 제외(길이 최소화)
    private String buildJwtByPhone(String phone, String roleValue, String usernameOrNull, String nameOrNull,
                                   long expiryEpochMillis, boolean access) {
        Map<String, Object> claims;
        if (access) {
            Map<String, Object> c = new HashMap<>();
            // subject가 phone이지만, 접근 편의를 위해 access에는 보조 클레임도 포함
            c.put(PHONE_CLAIM_KEY, phone);
            if (usernameOrNull != null && !usernameOrNull.isBlank()) c.put(USERNAME_CLAIM_KEY, usernameOrNull);
            if (nameOrNull != null && !nameOrNull.isBlank()) c.put(NAME_CLAIM_KEY, nameOrNull);
            claims = c;
        } else {
            // refresh는 sub/iss/iat/exp/jti(+서명)만 포함
            claims = Map.of();
        }

        String roleToInclude = access ? roleValue : null;
        return buildJwt(phone, claims, roleToInclude, expiryEpochMillis);
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

    // 현재 subject는 phone
    public String getUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public String getPhone(String token) {
        return getAllClaims(token).getSubject();
    }

    public Optional<String> getEmbeddedUsername(String token) {
        Object v = getAllClaims(token).get(USERNAME_CLAIM_KEY);
        if (v == null) return Optional.empty();
        String s = v.toString();
        return (s.isBlank() ? Optional.empty() : Optional.of(s));
    }

    public Optional<String> getEmbeddedName(String token) {
        Object v = getAllClaims(token).get(NAME_CLAIM_KEY);
        if (v == null) return Optional.empty();
        String s = v.toString();
        return (s.isBlank() ? Optional.empty() : Optional.of(s));
    }

    private Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    // ===== JWE 복호화(필요 시) =====
    public String decryptToken(String jweToken) {
        try {
            String useKey = (jweSecret != null && !jweSecret.isBlank()) ? jweSecret : secret;
            byte[] aesKeyBytes = useKey.getBytes(StandardCharsets.UTF_8);
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
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) return bearerToken.substring(7);
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
            Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
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