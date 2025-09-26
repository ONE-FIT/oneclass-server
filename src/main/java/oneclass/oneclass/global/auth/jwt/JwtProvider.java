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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

/**
 * JwtProvider
 * - Access/Refresh 토큰 생성
 * - Claims 파싱/역직렬화
 * - Role/Authorities 추출을 위한 기본 claim 파싱 지원 (Filter에서 활용)
 * - 필요 시 JWE(암호화) 토큰 복호화 지원
 */
@Component
public class JwtProvider {

    // ==== Claim Key 상수 ====
    public static final String ROLE_CLAIM_KEY = "role";      // 단일 역할
    public static final String ROLES_CLAIM_KEY = "roles";    // 다중 역할 (배열 또는 CSV 문자열)

    private final String secret;
    private final long accessValidityMillis;
    private final long refreshValidityMillis;   // 설정 없으면 access*2
    private Key key;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long accessValiditySeconds,
            @Value("${jwt.refresh-validity-in-seconds:0}") long refreshValiditySeconds
    ) {
        this.secret = secret;
        this.accessValidityMillis = accessValiditySeconds * 1000;
        this.refreshValidityMillis =
                (refreshValiditySeconds > 0 ? refreshValiditySeconds * 1000 : accessValiditySeconds * 2000);
    }

    @PostConstruct
    public void init() {
        // 최소 32바이트(256bit) 이상 권장 (HS256)
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret length must be >= 32 chars for HS256");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /* ================== 토큰 생성 ================== */

    /**
     * Access + Refresh 모두 발급 (기존 ResponseToken 유지)
     * - Refresh에는 role claim 기본적으로 넣지 않음 (최소한 정보 원칙)
     */
    public ResponseToken generateToken(String subject, String roleValue) {
        String access = generateAccessToken(subject, roleValue);
        String refresh = generateRefreshToken(subject);
        return new ResponseToken(access, refresh);
    }

    /**
     * 만료시간(epoch ms)까지 함께 담고 싶은 경우 확장 Pair 반환
     * (ResponseToken DTO를 확장하고 싶다면 별도 DTO 생성)
     */
    public TokenPair generateTokenPairWithExpiry(String subject, String roleValue) {
        long now = System.currentTimeMillis();
        long accessExp = now + accessValidityMillis;
        long refreshExp = now + refreshValidityMillis;

        String access = buildJwt(subject, roleValue, accessExp, true);
        String refresh = buildJwt(subject, null, refreshExp, false);
        return new TokenPair(
                access,
                refresh,
                accessExp,
                refreshExp
        );
    }

    /**
     * Access Token 단독 발급 (Refresh 재사용 정책에서 사용)
     */
    public String generateAccessToken(String subject, String roleValue) {
        long exp = System.currentTimeMillis() + accessValidityMillis;
        return buildJwt(subject, roleValue, exp, true);
    }

    private String generateRefreshToken(String subject) {
        long exp = System.currentTimeMillis() + refreshValidityMillis;
        return buildJwt(subject, null, exp, false);
    }

    /**
     * JWT 빌드 공통 로직
     * @param includeRole true일 때만 role claim 사용
     */
    private String buildJwt(String subject, String roleValue, long expiryEpochMillis, boolean includeRole) {
        Date now = new Date();
        Date exp = new Date(expiryEpochMillis);

        JwtBuilder builder = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(exp);

        if (includeRole && roleValue != null) {
            // roleValue 가 "ROLE_ADMIN" 형태인지 "ADMIN" 형태인지 통일 필요
            builder.claim(ROLE_CLAIM_KEY, roleValue);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /* ================== 검증/파싱 ================== */

    /**
     * 토큰 유효성 검증 (유효하면 true 반환, 실패 시 CustomException)
     */
    public boolean validateToken(String token) {
        try {
            parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    /**
     * Claims 전체 반환
     */
    public Claims getAllClaims(String token) {
        try {
            return parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // 만료된 토큰이라도 Claims 필요한 경우 여기에서 e.getClaims() 반환 가능:
            // return e.getClaims();
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    /**
     * Subject(username or academyCode)
     */
    public String getUsername(String token) {
        return getAllClaims(token).getSubject();
    }

    /**
     * 만료여부 (Claims 이미 구했을 때)
     */
    public boolean isExpired(Claims claims) {
        return claims.getExpiration() != null && claims.getExpiration().before(new Date());
    }

    public Date getExpiration(String token) {
        return getAllClaims(token).getExpiration();
    }

    private Jws<Claims> parseClaimsJws(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    /* ================== Role / Roles 추출 유틸 ================== */

    /**
     * roles/role claim 을 문자열 목록으로 통합 추출 (필터/서비스에서 활용)
     * - "role": "ROLE_ADMIN" 또는 "ADMIN"
     * - "roles": ["ADMIN","TEACHER"] / ["ROLE_ADMIN","ROLE_TEACHER"]
     * - "roles": "ADMIN,TEACHER"
     * 최종적으로 ROLE_ prefix 통일
     */
    public List<String> extractRoleStrings(String token) {
        Claims claims = getAllClaims(token);
        return extractRoleStrings(claims);
    }

    public List<String> extractRoleStrings(Claims claims) {
        Set<String> roleSet = new LinkedHashSet<>();

        Object single = claims.get(ROLE_CLAIM_KEY);
        if (single instanceof String s) {
            roleSet.add(normalizeRole(s));
        }

        Object multi = claims.get(ROLES_CLAIM_KEY);
        if (multi instanceof Collection<?> col) {
            for (Object o : col) {
                if (o != null) roleSet.add(normalizeRole(o.toString()));
            }
        } else if (multi instanceof String csv) {
            Arrays.stream(csv.split(","))
                    .map(String::trim)
                    .filter(v -> !v.isEmpty())
                    .forEach(v -> roleSet.add(normalizeRole(v)));
        }

        if (roleSet.isEmpty()) {
            // 기본 사용자 권한 (정책에 따라 제거/변경 가능)
            roleSet.add("ROLE_USER");
        }
        return new ArrayList<>(roleSet);
    }

    private String normalizeRole(String raw) {
        if (raw == null || raw.isBlank()) return "ROLE_USER";
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }

    /* ================== JWE 복호화 (선택) ================== */

    /**
     * 암호화된 JWE 토큰 복호화 (사용 중이 아니면 호출 안 해도 됨)
     */
    public String decryptToken(String jwtToken) throws Exception {
        byte[] aesKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        JWEObject jweObject = JWEObject.parse(jwtToken);
        jweObject.decrypt(new DirectDecrypter(aesKey.getEncoded()));
        return jweObject.getPayload().toString();
    }

    /* ================== HTTP 편의 ================== */

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /* ================== 확장용 내부 DTO ================== */

    /**
     * 확장된 토큰 페어 (만료시간 포함).
     * 기존 ResponseToken을 수정하기 어렵다면 별도로 사용.
     */
    public record TokenPair(
            String accessToken,
            String refreshToken,
            long accessTokenExpiresAt,   // epoch millis
            long refreshTokenExpiresAt
    ) {}
}