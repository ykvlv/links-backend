package dev.ykvlv.links.analytics.application.data.dto

data class IpRange(
    val start: UInt,
    val end: UInt,
    val asn: UInt,
    val country: String,
    val asnOrg: String
)
