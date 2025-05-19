package dev.ykvlv.links.analytics.application.config

import nl.basjes.parse.useragent.UserAgentAnalyzer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BeansConfig {

    @Bean
    fun userAgentAnalyzer(): UserAgentAnalyzer {
        return UserAgentAnalyzer
            .newBuilder()
            .withAllFields()
            .build()
    }

}
