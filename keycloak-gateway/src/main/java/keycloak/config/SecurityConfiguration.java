package keycloak.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http
    ) {
        http
            .cors()
            .and()
            .csrf()
            .disable()
            .authorizeExchange()
            .pathMatchers("/login/**", "/logout**")
            .permitAll()
            .pathMatchers("/test/user/**").hasRole("USER")
            .pathMatchers("/test/admin/**").hasRole("ADMIN")
            .anyExchange()
            .authenticated()
            .and()
            .oauth2Login(); // to redirect to oauth2 login page.

        return http.build();
    }
}
