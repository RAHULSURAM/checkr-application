package com.example.CheckrApplication.security;

import com.example.CheckrApplication.DAO.UserDAO;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    @Getter
    private Long id;
    @Getter
    private String name;
//    private String username;
    private final String email;
    private final String password;

    public UserPrincipal(UserDAO user) {
        this.id = user.getId();
        this.name = user.getFirstName() +" " + user.getLastName();
        this.email = user.getEmail();
        this.password = user.getPassword();
    }


    // No roles implemented; return null or empty list
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("USER"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
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
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}