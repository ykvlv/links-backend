package dev.ykvlv.links.analytics.application.data.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ClickEvent(
    val timestamp: String,
    val slug: String,
    @JsonProperty("resolved_url")
    val resolvedUrl: String,
    val ip: String,
    @JsonProperty("x_forwarded_for")
    val xForwardedFor: String,
    @JsonProperty("x_real_ip")
    val xRealIp: String,
    @JsonProperty("user_agent")
    val userAgent: String,
    @JsonProperty("accept_language")
    val acceptLanguage: String,
    val referer: String,
    val origin: String,
    val host: String,
    @JsonProperty("is_cache_hit")
    val isCacheHit: Boolean
)
