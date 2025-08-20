package fr.rguillemot.website.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
class SecurityConfig {

    /**
     * Config for dev environment:
     * ➝ CSRF disabled
     * ➝ Allows testing your API without hassle
     */
    @Bean
    @Profile("dev")
    fun securityFilterChainDev(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth.anyRequest().permitAll()
            }

        return http.build()
    }

}
