package oneclass.oneclass.auth.jwt;


import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
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
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
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
    //JWE 암호화
    public String encyptToken(String PlainJwt) throws Exception{
        byte[] aesKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        JWEHeader header = new JWEHeader(JWEAlgorithm.DIR, EncryptionMethod.A128CBC_HS256);
        Payload payload = new Payload(PlainJwt);
        JWEObject jweObject = new JWEObject(header, payload);
        jweObject.encrypt(new DirectEncrypter(aesKey.getEncoded()));
        return jweObject.serialize();
    }
    //JWE 복호화
    public String decyptToken(String jwtToken) throws Exception{
        byte[] aesKeyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        JWEObject jweObject = JWEObject.parse(jwtToken);
        jweObject.decrypt(new DirectDecrypter(aesKey.getEncoded()));
        return jweObject.getPayload().toString();
    }
    //토큰 생성(JWE 적용)
    public ResponseToken generateTokenJwe(String username){
        Date now = new Date();
        //aceess token 생성
        Date accessExpiry = new Date(now.getTime() + tokenValidityInMilliseconds);
        String accessToken = Jwts.builder()
                            .setSubject(username)
                            .setIssuedAt(now)
                            .setExpiration(accessExpiry)
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();

        //refresh token 생성
        Date refreshExpiry = new Date(now.getTime() + tokenValidityInMilliseconds * 2);
        String refreshToken = Jwts.builder()
                            .setSubject(username)
                            .setIssuedAt(now)
                            .setExpiration(refreshExpiry)
                            .signWith(key, SignatureAlgorithm.HS256)
                            .compact();
        //암호화 적용
        String encryptedAccessToken;
        String encryptedRefreshToken;

        try{
            encryptedAccessToken = encyptToken(accessToken);
            encryptedRefreshToken = encyptToken(refreshToken);
        }catch (Exception e){
            throw new RuntimeException("토큰 암호화 실패",e);
        }
        return new ResponseToken(encryptedAccessToken, encryptedRefreshToken);
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
