package fr.rguillemot.website.backend

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class CorsConfig(
    private val env: Environment,
    @Value("\${frontend.url}") private val frontendUrl: String
) : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        val isDev = env.activeProfiles.contains("dev")

        registry.addMapping("/**")
            .apply {
                if (isDev) {
                    allowedOriginPatterns("*")
                } else {
                    allowedOrigins(frontendUrl)
                }
            }
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}
