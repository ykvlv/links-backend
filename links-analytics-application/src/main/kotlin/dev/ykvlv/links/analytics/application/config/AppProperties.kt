package dev.ykvlv.links.analytics.application.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "app")
class AppProperties {

    lateinit var ip2asnRelPath: String
    lateinit var ip2asnUrl: String
    var ip2asnAutoUpdate: Boolean = false

    lateinit var kafkaTopicClicks: String

}
