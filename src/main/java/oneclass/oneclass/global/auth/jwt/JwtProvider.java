package oneclass.oneclass.global.auth.jwt;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import oneclass.oneclass.domain.member.dto.response.ResponseToken;
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
    public static final String PHONE_CLAIM_KEY = "phone";
    public static final String USERNAME_CLAIM_KEY = "username";
    public static final String NAME_CLAIM_KEY = "name";
    public static final String ID_CLAIM_KEY = "id"; // 추가된 키

    private final String secret;
    private final String jweSecret;
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

    // ===== 기존 서비스(Academy, Member) 호환용 메서드 (수정 금지) =====
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

    // ===== 새로운 ID 포함 메서드 (로그인용) =====
    public ResponseToken generateTokenByUsername(Long id, String username, String roleValue, String phoneOrNull, String nameOrNull) {
        long now = System.currentTimeMillis();
        String access = buildJwtByUsername(id, username, roleValue, phoneOrNull, nameOrNull, now + accessValidityMillis, true);
        String refresh = buildJwtByUsername(id, username, null, phoneOrNull, nameOrNull, now + refreshValidityMillis, false);
        return new ResponseToken(access, refresh);
    }

    // 내부 빌더 (ID 포함)
    private String buildJwtByUsername(Long id, String username, String roleValue, String phoneOrNull, String nameOrNull,
                                      long expiryEpochMillis, boolean access) {
        Map<String, Object> claims = new HashMap<>();
        if (id != null) claims.put(ID_CLAIM_KEY, id); // ID 삽입

        if (access) {
            if (phoneOrNull != null && !phoneOrNull.isBlank()) claims.put(PHONE_CLAIM_KEY, phoneOrNull);
            if (nameOrNull != null && !nameOrNull.isBlank()) claims.put(NAME_CLAIM_KEY, nameOrNull);
            if (username != null && !username.isBlank()) claims.put(USERNAME_CLAIM_KEY, username);
        }

        String roleToInclude = access ? roleValue : null;
        return buildJwt(username, claims, roleToInclude, expiryEpochMillis);
    }

    // 최하단 공통 빌더
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

    // ===== 검증 및 정보 추출 (기존 에러 해결용) =====
    public String getUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    public Claims getAllClaims(String token) {
        try {
            return parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (Exception e) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    private Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public String decryptToken(String jweToken) {
        try {
            String useKey = (jweSecret != null && !jweSecret.isBlank()) ? jweSecret : secret;
            byte[] aesKeyBytes = useKey.getBytes(StandardCharsets.UTF_8);
            JWEObject jweObject = JWEObject.parse(jweToken);
            jweObject.decrypt(new DirectDecrypter(aesKeyBytes));
            return jweObject.getPayload().toString();
        } catch (Exception e) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) return bearerToken.substring(7);
        return null;
    }

    public boolean isTokenInvalid(String token) {
        try {
            parseClaimsJws(token);
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    // ✅ 이 메서드가 누락되어 에러가 발생했습니다. 아래 내용을 추가하세요.
    public String generateAccessTokenByUsername(Long id, String username, String roleValue, String phoneOrNull, String nameOrNull) {
        return buildJwtByUsername(id, username, roleValue, phoneOrNull, nameOrNull,
                System.currentTimeMillis() + accessValidityMillis, true);
    }
}