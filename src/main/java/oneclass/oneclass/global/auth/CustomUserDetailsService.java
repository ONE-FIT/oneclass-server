package oneclass.oneclass.global.auth;

import lombok.RequiredArgsConstructor;
import oneclass.oneclass.domain.member.entity.Member;
import oneclass.oneclass.domain.member.repository.MemberRepository;
import oneclass.oneclass.domain.academy.entity.Academy;
import oneclass.oneclass.domain.academy.repository.AcademyRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final AcademyRepository academyRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Member 우선
        Member member = memberRepository.findByUsername(username).orElse(null);
        if (member != null) {
            String role = normalizeRole(member.getRole().name());
            return CustomUserDetails.forMember(
                    member.getId(),
                    member.getUsername(),
                    member.getPassword(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        }

        // 2. Academy
        Academy academy = academyRepository.findByAcademyCode(username).orElse(null);
        if (academy != null) {
            String role = normalizeRole(academy.getRole().name());
            return CustomUserDetails.forAcademy(
                    academy.getAcademyCode(),
                    academy.getPassword(),
                    List.of(new SimpleGrantedAuthority(role))
            );
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }

    private String normalizeRole(String raw) {
        return raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
    }
}