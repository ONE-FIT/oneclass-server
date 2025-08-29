package oneclass.oneclass.auth.service;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.auth.dto.ResponseToken;
import oneclass.oneclass.auth.dto.SignupRequest;
import oneclass.oneclass.auth.entity.Member;
import oneclass.oneclass.auth.entity.RefreshToken;
import oneclass.oneclass.auth.entity.Role;
import oneclass.oneclass.auth.jwt.JWTProvider;
import oneclass.oneclass.auth.repository.MemberRepository;
import oneclass.oneclass.auth.repository.RefreshTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    @Override
    public void signup(SignupRequest request){
        Role selectRole = request.getRole();
        if(selectRole == null){
            selectRole = Role.STUDENT;//선택 안하면 기본 STUDENT
        }else if(!(selectRole == Role.STUDENT ||  selectRole == Role.PARENT)){
            throw new IllegalArgumentException("허용되지 않은 유형입니다.");
        }
        //회원가입 중복체크
        if (memberRepository.findByUsername(request.getUsername()).isPresent()){
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }
        //비번 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //Entity 생성 및 저장
        Member member = new Member();
        member.setRole(selectRole);
        member.setUsername(request.getUsername());
        member.setPassword(encodedPassword);
        member.setEmail(request.getEmail());
        member.setPhone(request.getPhone());
        memberRepository.save(member);
    }

    @Override
    public ResponseToken login(String username, String password){
        //회원정보 조회 with ID
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        //비번 체크
        if (!passwordEncoder.matches(password, member.getPassword())){
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        // 토큰 생성 시
        String roleClaim = "ROLE_" + member.getRole().name();
        ResponseToken tokens = jwtProvider.generateToken(member.getUsername(), roleClaim);

        refreshTokenRepository.deleteByUsername(username);
        RefreshToken refreshToken = RefreshToken.builder()
                .username(username)
                .token(tokens.getRefreshToken())
                .expiryDate(LocalDateTime.now().plusDays(28))
                .build();
        refreshTokenRepository.save(refreshToken);

        return tokens;

    }

    @Override
    public String findUsername(String emailOrPhone) {
        // 이메일 또는 전화번호로 회원 찾기
        Member member = memberRepository.findByEmailOrPhone(emailOrPhone, emailOrPhone)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        return member.getUsername();
    }
    @Override
    public void sendResetPasswordEmail(String usernameOrEmail) {
        Member member = memberRepository.findByEmailOrPhone(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));
        String tempCode = UUID.randomUUID().toString().substring(0, 6);
        emailService.sendSimpleMail(member.getEmail(),"비밀번호 재설정", "인증코드: " + tempCode);
    }

    @Override
    public void resetPassword(String username, String newPassword, String verificationCode) {
        //비번 재설정
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new  IllegalArgumentException("존재하지 않는 아이디입니다."));
        member.setPassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }
}
