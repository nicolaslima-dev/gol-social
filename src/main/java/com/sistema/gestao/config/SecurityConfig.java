package com.sistema.gestao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // 1. ARQUIVOS ESTÁTICOS
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/img/**", "/webjars/**").permitAll()

                        // 2. TELA DE LOGIN
                        .requestMatchers("/auth/**", "/login").permitAll()

                        // 3. REGRAS DE ADMIN
                        .requestMatchers(
                                "/configuracoes/**",
                                "/usuarios/**",
                                "/instituicao/**",
                                "/backup/**",
                                "/inscritos/**",    // <--- Adicionado
                                "/turmas/**",       // <--- Adicionado
                                "/funcionarios/**"  // <--- Adicionado
                        ).hasRole("ADMIN")

                        // 4. RESTO DO SISTEMA (Dashboard, Frequencia, Atividades)
                        // Fica liberado para ADMIN e PROFESSOR
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