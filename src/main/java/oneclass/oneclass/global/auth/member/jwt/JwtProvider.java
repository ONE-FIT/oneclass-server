package oneclass.oneclass.global.auth.member.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
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
    private final long tokenValidityInMilliseconds;
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

    // 토큰 생성
    public ResponseToken generateToken(String username, String role) {
        Date now = new Date();

        Date accessExpiry = new Date(now.getTime() + tokenValidityInMilliseconds);
        JwtBuilder accessBuilder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(accessExpiry);
        if (role != null) {
            accessBuilder.claim("role", role);
        }
        String accessToken = accessBuilder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        Date refreshExpiry = new Date(now.getTime() + tokenValidityInMilliseconds * 2);
        String refreshToken = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(refreshExpiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return new ResponseToken(accessToken, refreshToken);
    }

    public String generateAccessToken(String username, String role) {
        Date now = new Date();
        Date accessExpiry = new Date(now.getTime() + tokenValidityInMilliseconds);

        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(accessExpiry);

        if (role != null) {
            builder.claim("role", role);
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }


    // JWE 복호화
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

    // username 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //로그아웃 로직
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}