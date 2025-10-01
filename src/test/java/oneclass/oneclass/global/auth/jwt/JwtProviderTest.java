package oneclass.oneclass.global.auth.jwt;

import oneclass.oneclass.domain.member.dto.ResponseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class JwtProviderTest {

    private JwtProvider jwtProvider;


    @BeforeEach
    void setUp() {
        // secret은 최소 32글자 이상이어야 함
        String secret = "abcdefghijklmnopqrstuvwxyz123456";
        long accessValiditySeconds = 3600; // 1시간
        long refreshValiditySeconds = 7200; // 2시간

        jwtProvider = new JwtProvider(secret, accessValiditySeconds, refreshValiditySeconds);
        jwtProvider.init(); // @PostConstruct 직접 호출
    }

    @Test
    public void ProvideTest() {

        ResponseToken responseToken = jwtProvider.generateToken("FineFinee", "USER");
        System.out.println(responseToken.toString());

        assertThat(responseToken).isNotNull();
        assertThat(responseToken.getAccessToken()).isNotNull();
        assertThat(responseToken.getRefreshToken()).isNotNull();
    }

}
