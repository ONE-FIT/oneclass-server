package oneclass.oneclass.auth.jwt;


import io.jsonwebtoken.*;
import jakarta.annotation.PostConstruct;
import oneclass.oneclass.auth.dto.ResponseToken;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


@Component
public class JWTProvider implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(JWTProvider.class);
    private static final String AUTHORITIES_KEY = "auth";
    private final String secret;
    private final long tokenValidityInMilliseconds;
    private Key key;

    public JWTProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.token-validity-in-seconds}") long tokenValidityInSeconds){
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInSeconds * 1000;

    }
    @PostConstruct
    public void afterPropertiesSet() throws Exception {
        this.key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
    //토큰생성
    public ResponseToken generateToken(String username){
        Date now = new Date();

        //accessToken
        Date accessExpiry = new Date(now.getTime() + tokenValidityInMilliseconds); //14일 후 만료
        String accessToken = Jwts.builder()
                            .setSubject(username)
                            .setIssuedAt(now)
                            .setExpiration(accessExpiry)
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();
        //refreshToken
        Date refreshExpiry = new Date(now.getTime() + tokenValidityInMilliseconds * 2); //28일 후 만료
        String refreshToken = Jwts.builder()
                            .setSubject(username)
                            .setIssuedAt(now)
                            .setExpiration(refreshExpiry)
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();
        return new ResponseToken(accessToken, refreshToken);
    }

    //토큰검증
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }catch (ExpiredJwtException e){
            logger.info("만료된 JWT");
        }catch (IllegalArgumentException e){
            logger.info("잘못된 토큰");
        }
        return false;
    }
    public String getUsername(String token){
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

}
