package rw.ac.auca.ecommerce.core.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // You can re-enable CSRF later with token support
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/auth/**",
                                "/customer/**",
                                "/seller/**",
                                "/product/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/admin/**"
                        ).permitAll()

                        .requestMatchers("/cart/**", "/orders/**","/seller/dashboard/**","admin/login/**","admin/dashboard/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cart/**", "/orders/**","/seller/dashboard/**","admin/login/**","admin/dashboard/**").permitAll()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable());

        return http.build();
    }
}
