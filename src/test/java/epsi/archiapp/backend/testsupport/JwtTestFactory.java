package epsi.archiapp.backend.testsupport;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.List;
import java.util.Map;

public final class JwtTestFactory {

    private JwtTestFactory() {
    }

    public static JwtRequestPostProcessor userJwt(String username) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(username)
                        .claim("preferred_username", username)
                        .claim("email", username + "@example.test")
                        .claim("realm_access", Map.of("roles", List.of("USER"))))
                .authorities(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public static JwtRequestPostProcessor adminJwt(String username) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject(username)
                        .claim("preferred_username", username)
                        .claim("email", username + "@example.test")
                        .claim("realm_access", Map.of("roles", List.of("ADMIN", "USER"))))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
    }
}

