package fr.rguillemot.website.backend.authms

import fr.rguillemot.website.backend.authms.config.WebAuthnConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(WebAuthnConfig::class)
class BackendRguillemotApplication

fun main(args: Array<String>) {
    runApplication<BackendRguillemotApplication>(*args)
}
