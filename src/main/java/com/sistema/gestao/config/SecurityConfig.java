package com.sistema.gestao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // LIBERA ARQUIVOS ESTÁTICOS
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/webjars/**").permitAll()

                        // LIBERA AS ROTAS DE AUTENTICAÇÃO
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/login").permitAll()

                        // BLOQUEIOS POR PERFIL
                        .requestMatchers("/funcionarios/**").hasRole("ADMIN")
                        .requestMatchers("/frequencia/**").hasAnyRole("ADMIN", "PROFESSOR")

                        // RESTO BLOQUEADO
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}