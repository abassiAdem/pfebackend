package com.example.demo.config;
import java.util.List;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.demo.security.JwtConverter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebConfig {

	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
	private String issuerUri;

	private final JwtConverter jwtConverter;
	public WebConfig(JwtConverter jwtConverter){
		this.jwtConverter=jwtConverter;
	}
	  @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
		        .cors(cors -> cors.configurationSource(corsConfigurationSource())) 
		        .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                		.requestMatchers("/api/auth/**").permitAll() 
                        .requestMatchers("/api/admin/**").authenticated()
                        .anyRequest().permitAll() 
                    )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                            
                            .jwtAuthenticationConverter(jwtConverter.jwtAuthenticationConverter())
                        )
                    )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
    }
	    @Bean
	    public CorsConfigurationSource corsConfigurationSource() {
	        CorsConfiguration config = new CorsConfiguration();
	        
	        config.setAllowedOrigins(List.of(
	            "http://localhost:5174"

	        ));
	        
	        config.setAllowedMethods(List.of(
	            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
	        ));
	        
	        config.setAllowedHeaders(List.of(
	            "Authorization",
	            "Cache-Control",
	            "Content-Type",
	            "Accept",
	            "X-XSRF-TOKEN",
	            "X-Requested-With"
	        ));
	        
	        config.setExposedHeaders(List.of(
	            "Authorization",
	            "X-XSRF-TOKEN",
	            "Set-Cookie"
	        ));
	        
	        config.setAllowCredentials(true);
	        
	        

	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration("/**", config);
	        return source;
	    }
}
