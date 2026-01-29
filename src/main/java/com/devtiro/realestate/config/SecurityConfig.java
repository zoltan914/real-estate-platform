package com.devtiro.realestate.config;

import com.devtiro.realestate.domain.dto.AttemptInfo;
import com.devtiro.realestate.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${security.password.bcrypt-strength}")
    private int passwordBcryptStrength;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll() // TODO admin role

                        .requestMatchers(HttpMethod.POST, "/api/listings").hasRole("AGENT")
                        .requestMatchers(HttpMethod.POST, "/api/listings/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/listings/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.DELETE, "/api/listings/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/listings/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.GET, "/api/listings/my-listings").hasRole("AGENT")
                        .requestMatchers(HttpMethod.GET, "/api/listings/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listings").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/viewings/user/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/viewings/agent/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.PATCH, "/api/viewings/agent/**").hasRole("AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/viewings/*/reschedule").hasAnyRole("AGENT","USER")
                        .requestMatchers(HttpMethod.PUT, "/api/viewings/*/cancel").hasAnyRole("AGENT","USER")
                        .requestMatchers(HttpMethod.PUT, "/api/viewings/*/status").hasRole("AGENT")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(passwordBcryptStrength);
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(authProvider);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React dev server
                "https://yourdomain.com"   // Production
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public ConcurrentHashMap<String, AttemptInfo> attemptsCache() {
        return new ConcurrentHashMap<>();
    }
}

