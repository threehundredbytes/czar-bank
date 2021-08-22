package ru.dreadblade.czarbank.domain.security;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String userId;

    private String username;

    private String password;

    private String email;

    @Singular
    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
    @JoinTable(name = "user_role",
            joinColumns = { @JoinColumn(name = "USER_ID", referencedColumnName = "ID") },
            inverseJoinColumns = { @JoinColumn(name = "ROLE_ID", referencedColumnName = "ID" ) })
    private Set<Role> roles;

    private boolean isAccountExpired;
    private boolean isAccountLocked;
    private boolean isCredentialsExpired;
    private boolean isEnabled;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toSet());

        authorities.addAll(roles.stream()
                .map(Role::getPermissions)
                .flatMap(Set::stream)
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet()));

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isAccountExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isAccountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !isCredentialsExpired;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
