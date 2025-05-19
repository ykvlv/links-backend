package dev.ykvlv.links.analytics.application.data.dto

data class ClickEvent(
    val timestamp: String,
    val slug: String,
    val resolved_url: String,
    val ip: String,
    val x_forwarded_for: String,
    val x_real_ip: String,
    val user_agent: String,
    val accept_language: String,
    val referer: String,
    val origin: String,
    val host: String,
    val is_cache_hit: Boolean
)
