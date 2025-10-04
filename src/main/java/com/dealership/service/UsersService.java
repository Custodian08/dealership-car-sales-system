package com.dealership.service;

import com.dealership.dto.UserAccountDto;
import com.dealership.dto.UserUpsertRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {
    private final UserDetailsManager users;
    private final JdbcTemplate jdbc;

    public UsersService(UserDetailsManager users, JdbcTemplate jdbcTemplate) {
        this.users = users;
        this.jdbc = jdbcTemplate;
    }

    public List<UserAccountDto> list() {
        // Fetch all usernames
        List<String> usernames = jdbc.query("select username from users order by username", (rs, i) -> rs.getString(1));
        List<UserAccountDto> out = new ArrayList<>();
        for (String u : usernames) {
            List<String> roles = jdbc.query(
                    "select authority from authorities where username = ? order by authority",
                    (rs, i) -> rs.getString(1), u
            ).stream().map(a -> a != null && a.startsWith("ROLE_") ? a.substring(5) : a).toList();
            out.add(new UserAccountDto(u, roles));
        }
        return out;
    }

    public void create(UserUpsertRequest req) {
        if (req == null || req.username() == null || req.username().isBlank()) throw new IllegalArgumentException("Username is required");
        String pwd = req.password() != null ? req.password() : "";
        String[] roles = req.roles() != null ? req.roles().toArray(new String[0]) : new String[]{};
        User.UserBuilder b = User.withUsername(req.username()).password("{noop}" + pwd).roles(roles);
        if (req.enabled() != null && !req.enabled()) b.disabled(true);
        users.createUser(b.build());
    }

    public void update(String username, UserUpsertRequest req) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (!users.userExists(username)) throw new IllegalArgumentException("User not found");
        UserDetails existing = users.loadUserByUsername(username);
        List<String> rolesList = req != null && req.roles() != null ? req.roles() : existing.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).map(a -> a.startsWith("ROLE_") ? a.substring(5) : a).toList();
        String[] roles = rolesList.toArray(new String[0]);
        String rawPwd = (req != null && req.password() != null) ? req.password() : existing.getPassword();
        boolean enabled = req != null && req.enabled() != null ? req.enabled() : existing.isEnabled();
        // rawPwd may already have {noop} prefix if from existing; ensure prefix present exactly once
        String pwdToUse = rawPwd.startsWith("{noop}") ? rawPwd : ("{noop}" + rawPwd);
        User.UserBuilder b = User.withUsername(username).password(pwdToUse).roles(roles);
        if (!enabled) b.disabled(true);
        users.updateUser(b.build());
    }

    public void delete(String username) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required");
        if (!users.userExists(username)) return;
        users.deleteUser(username);
    }
}
