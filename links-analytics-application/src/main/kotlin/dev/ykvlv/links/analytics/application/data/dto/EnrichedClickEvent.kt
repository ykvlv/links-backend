package dev.ykvlv.links.analytics.application.data.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class EnrichedClickEvent(
    val timestamp: String,
    @JsonProperty("event_date")
    val eventDate: String,
    @JsonProperty("event_hour")
    val eventHour: String,
    @JsonProperty("event_dow")
    val eventDow: String,
    val slug: String,
    @JsonProperty("resolved_url")
    val resolvedUrl: String,
    val ip: String,
    @JsonProperty("country_code")
    val countryCode: String?,
    @JsonProperty("country_name")
    val countryName: String?,
    @JsonProperty("city_name")
    val cityName: String?,
    val asn: UInt?,
    @JsonProperty("asn_org")
    val asnOrg: String?,
    val browser: String?,
    val os: String?,
    @JsonProperty("device_type")
    val deviceType: String?,
    @JsonProperty("device_brand")
    val deviceBrand: String?,
    @JsonProperty("device_name")
    val deviceName: String?,
    @JsonProperty("accept_language")
    val acceptLanguage: String,
    val referer: String,
    @JsonProperty("referer_host")
    val refererHost: String?,
    @JsonProperty("referer_path")
    val refererPath: String?,
    val origin: String,
    val host: String,
    @JsonProperty("is_cache_hit")
    val isCacheHit: Boolean,
    @JsonProperty("client_fingerprint")
    val clientFingerprint: String,
    @JsonProperty("is_private_ip")
    val isPrivateIp: Boolean
)
