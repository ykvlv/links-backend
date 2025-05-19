package dev.ykvlv.links.analytics.application.data.dto

data class IpRange(
    val start: Long,
    val end: Long,
    val asn: Long,
    val country: String,
    val asnOrg: String
)
