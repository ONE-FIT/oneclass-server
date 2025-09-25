package oneclass.oneclass.global.auth.member.jwt;

import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.DirectDecrypter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import oneclass.oneclass.global.auth.member.dto.ResponseToken;
import oneclass.oneclass.global.auth.member.error.TokenError;
import oneclass.oneclass.global.exception.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {

    private final String secret;
    private final long tokenValidityInMilliseconds; // Access Token 유효 기간(ms)
    private Key key;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds
    ) {
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Access + Refresh 모두 발급.
     * Refresh 토큰 만료는 기존 정책과 동일하게 access * 2
     */
    public ResponseToken generateToken(String username, String role) {
        String accessToken = generateAccessToken(username, role);
        String refreshToken = generateRefreshToken(username); // role claim 일반적으로 refresh에는 넣지 않음
        return new ResponseToken(accessToken, refreshToken);
    }

    /**
     * Access Token 단독 발급 (Refresh 재사용 정책에서 사용)
     */
    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date accessExpiry = new Date(now.getTime() + tokenValidityInMilliseconds);

        JwtBuilder builder = baseBuilder(username, now, accessExpiry);
        if (role != null) {
            builder.claim("role", role);
        }
        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /**
     * Refresh Token 생성 (role claim 불필요 - 최소 정보만)
     */
    private String generateRefreshToken(String username) {
        Date now = new Date();
        Date refreshExpiry = new Date(now.getTime() + (tokenValidityInMilliseconds * 2));
        return baseBuilder(username, now, refreshExpiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 공통 빌더 (subject / issuedAt / expiration)
     */
    private JwtBuilder baseBuilder(String username, Date issuedAt, Date expiry) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(issuedAt)
                .setExpiration(expiry);
    }

    // JWE 복호화 (현재 사용 중이면 유지, 아니라면 제거 고려)
    public String decryptToken(String jwtToken) throws Exception {
        byte[] aesKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        JWEObject jweObject = JWEObject.parse(jwtToken);
        jweObject.decrypt(new DirectDecrypter(aesKey.getEncoded()));
        return jweObject.getPayload().toString();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new CustomException(TokenError.TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new CustomException(TokenError.UNAUTHORIZED);
        }
    }

    // username(subject) 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // Authorization 헤더에서 Bearer 토큰 추출
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}