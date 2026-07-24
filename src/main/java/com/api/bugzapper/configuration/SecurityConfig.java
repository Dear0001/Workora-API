package com.api.bugzapper.configuration;

import com.api.bugzapper.security.JwtAuthEntrypoint;
import com.api.bugzapper.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthEntrypoint jwtAuthEntrypoint;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, JwtAuthEntrypoint jwtAuthEntrypoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtAuthEntrypoint = jwtAuthEntrypoint;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(
                                "/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/files/**",
                                "/api/v1/auths/send-email",
                                "/api/v1/auths/local-login",
                                "/api/v1/auths/verify",
                                "/api/v1/auths/change-password",
                                "/api/v1/auths/google-auth",
                                "/api/v1/auths/local-register",
                                "/api/v1/auths/refresh"
                        ).permitAll()
                        .requestMatchers(
                                "/api/v1/company/**",
                                "/api/v1/project/**",
                                "/api/v1/notification/**",
                                "/api/v1/phases/**",
                                "/api/v1/phaseAttachment/**",
                                "/api/v1/apply/**",
                                "/api/v1/reports/**",
                                "/api/v1/project/**",
                                "/api/v1/phases/**",
                                "/api/v1/dashboard/**",
                                "/api/v1/role/**",
                                "/api/v1/users/**",
                                "/api/v1/tasks/**",
                                "/api/v1/rateFeedback/**",
                                "/api/v1/reports/**",
                                "/api/v1/newsfeed/**",
                                "/api/v1/auths/reset",
                                "/api/v1/pushNotification/**"
                        ).authenticated()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntrypoint))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
