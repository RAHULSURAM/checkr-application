package com.example.CheckrApplication.security;

import com.example.CheckrApplication.DAO.UserDAO;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class UserPrincipal implements UserDetails {

    @Getter
    private Long id;
    @Getter
    private String name;
//    private String username;
    private final String email;
    private final String password;

    public UserPrincipal(UserDAO user) {
        log.info("Started & Ended constructor UserPrincipal in UserPrincipal");
        this.id = user.getId();
        this.name = user.getFirstName() +" " + user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
    }


    // No roles implemented; return null or empty list
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("Started & Ended getAuthorities in UserPrincipal");
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        log.info("Started & Ended getPassword in UserPrincipal");
        return password;
    }

    @Override
    public String getUsername() {
        log.info("Started & Ended getUsername in UserPrincipal");
        return email;
    }

    // Username can be username or email based on login
//    @Override
//    public String getUsername() {
//        return username;
//    }

    // Account status
    @Override
    public boolean isAccountNonExpired() {
        log.info("Started & Ended isAccountNonExpired in UserPrincipal");
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        log.info("Started & Ended isAccountNonLocked in UserPrincipal");
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        log.info("Started & Ended isCredentialsNonExpired in UserPrincipal");
        return true;
    }

    @Override
    public boolean isEnabled() {
        log.info("Started & Ended isEnabled in UserPrincipal");
        return true;
    }
}