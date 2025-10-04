package com.dealership.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration
public class SecurityDataInitializer {
    private final JdbcTemplate jdbc;
    private final UserDetailsManager udm;

    public SecurityDataInitializer(JdbcTemplate jdbc, UserDetailsManager udm) {
        this.jdbc = jdbc;
        this.udm = udm;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seedUsersIfEmpty() {
        try {
            Integer count = jdbc.queryForObject("select count(*) from users", Integer.class);
            if (count == null || count == 0) {
                // Ensure admin exists
                if (!udm.userExists("admin")) {
                    udm.createUser(User.withUsername("admin").password("{noop}admin").roles("ADMIN").build());
                }
                // Couple of basic demo users
                if (!udm.userExists("emp")) {
                    udm.createUser(User.withUsername("emp").password("{noop}emp").roles("EMPLOYEE").build());
                }
                if (!udm.userExists("seller")) {
                    udm.createUser(User.withUsername("seller").password("{noop}seller").roles("SALESPERSON").build());
                }
            }
        } catch (Exception ignored) { /* table may not exist yet; Flyway will seed */ }
    }
}
