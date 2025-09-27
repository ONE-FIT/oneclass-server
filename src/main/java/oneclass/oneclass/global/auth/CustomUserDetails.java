package oneclass.oneclass.global.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

@Getter
public class CustomUserDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public enum AccountType { MEMBER, ACADEMY }

    private final Long id;                 // MEMBER면 PK, ACADEMY면 null
    private final String username;         // member.username 또는 academyCode
    private final String password;         // 암호 해시
    private final AccountType accountType;
    private final List<GrantedAuthority> authorities;
    private final boolean enabled;

    private CustomUserDetails(Long id,
                              String username,
                              String password,
                              AccountType accountType,
                              List<GrantedAuthority> authorities,
                              boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.accountType = accountType;
        this.authorities = authorities == null
                ? List.of()
                : Collections.unmodifiableList(authorities);
        this.enabled = enabled;
    }

    /* ========= static factory ========= */

    public static CustomUserDetails forMember(Long id,
                                              String username,
                                              String password,
                                              List<GrantedAuthority> authorities) {
        return new CustomUserDetails(id, username, password, AccountType.MEMBER, authorities, true);
    }

    public static CustomUserDetails forAcademy(String academyCode,
                                               String password,
                                               List<GrantedAuthority> authorities) {
        return new CustomUserDetails(null, academyCode, password, AccountType.ACADEMY, authorities, true);
    }

    /* ========= UserDetails 인터페이스 ========= */

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return username; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }

    /* ========= 편의 ========= */
    public boolean isMember()  { return accountType == AccountType.MEMBER; }
    public boolean isAcademy() { return accountType == AccountType.ACADEMY; }

    public Long getUserId() { return id; }

    /* ========= equals / hashCode (타입 + 식별자 기준) ========= */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails that)) return false;
        return accountType == that.accountType &&
                Objects.equals(id, that.id) &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountType, id, username);
    }

    @Override
    public String toString() {
        return "CustomUserDetails{type=%s, id=%s, username='%s', roles=%s}"
                .formatted(accountType, id, username, authorities);
    }
}