package keycloak.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfiguration {

    @Autowired(required = false)
    private ReactiveClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http
    ) {
        ServerHttpSecurity.AuthorizeExchangeSpec authorizeExchange = http
            .cors()
            .and()
            .csrf()
            .disable()
            .authorizeExchange()
            .pathMatchers("/login/**", "/logout**")
            .permitAll();

        // OAuth2 클라이언트 설정이 있을 때만 OAuth2 로그인 및 인증 활성화
        if (clientRegistrationRepository != null) {
            authorizeExchange
                .pathMatchers("/test/user/**").hasRole("USER")
                .pathMatchers("/test/admin/**").hasRole("ADMIN")
                .anyExchange()
                .authenticated();
            http.oauth2Login(); // to redirect to oauth2 login page.
        } else {
            // OAuth2가 설정되지 않은 경우 모든 요청 허용
            authorizeExchange.anyExchange().permitAll();
        }

        return http.build();
    }
}
