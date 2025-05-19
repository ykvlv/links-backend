package dev.ykvlv.links.analytics.application.service

import com.neovisionaries.i18n.CountryCode
import dev.ykvlv.links.analytics.application.data.dto.ClickEvent
import dev.ykvlv.links.analytics.application.data.dto.EnrichedClickEvent
import dev.ykvlv.links.analytics.application.utils.IpToAsnLookup
import nl.basjes.parse.useragent.UserAgent
import nl.basjes.parse.useragent.UserAgentAnalyzer
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId

@Service
class EnrichmentService(
    private val uaParser: UserAgentAnalyzer,
    private val ipToAsnLookup: IpToAsnLookup
) {
    fun enrich(event: ClickEvent): EnrichedClickEvent {
        // 1. Event timestamp
        val instant = Instant.parse(event.timestamp)
        val zoned = instant.atZone(ZoneId.of("UTC"))
        val eventDate = zoned.toLocalDate().toString()
        val eventHour = "%02d".format(zoned.hour)
        val eventDow = "%02d".format(zoned.dayOfWeek.value)

        // 2. IP -> ASN + country_code
        val ipInfo = ipToAsnLookup.lookup(event.ip)
        val countryCode = ipInfo?.country
        val countryName = countryCode?.let { CountryCode.getByAlpha2Code(it)?.name }
        val asn = ipInfo?.asn
        val asnOrg = ipInfo?.asnOrg

        // 3. UA parse
        val ua = uaParser.parse(event.userAgent)
        val browser = ua.getValue(UserAgent.AGENT_NAME)
        val os = ua.getValue(UserAgent.OPERATING_SYSTEM_NAME)
        val deviceType = ua.getValue(UserAgent.DEVICE_CLASS)
        val deviceBrand = ua.getValue(UserAgent.DEVICE_BRAND)
        val deviceName = ua.getValue(UserAgent.DEVICE_NAME)

        // 4. Referer
        val refererHost = try {
            java.net.URI(event.referer).host
        } catch (_: Exception) { null }
        val refererPath = try {
            java.net.URI(event.referer).path
        } catch (_: Exception) { null }

        // 5. Fingerprint
        val digest = MessageDigest.getInstance("SHA-1")
        val fpInput = "${event.ip}|${event.userAgent}"
        val clientFingerprint = digest.digest(fpInput.toByteArray()).joinToString("") { "%02x".format(it) }
        val isPrivate = isPrivateIp(event.ip)

        return EnrichedClickEvent(
            timestamp = event.timestamp,
            eventDate = eventDate,
            eventHour = eventHour,
            eventDow = eventDow,
            slug = event.slug,
            resolvedUrl = event.resolvedUrl,
            ip = event.ip,
            countryCode = countryCode,
            countryName = countryName,
            cityName = null,
            asn = asn,
            asnOrg = asnOrg,
            browser = browser,
            os = os,
            deviceType = deviceType,
            deviceBrand = deviceBrand,
            deviceName = deviceName,
            acceptLanguage = event.acceptLanguage,
            referer = event.referer,
            refererHost = refererHost,
            refererPath = refererPath,
            origin = event.origin,
            host = event.host,
            isCacheHit = event.isCacheHit,
            clientFingerprint = clientFingerprint,
            isPrivateIp = isPrivate
        )
    }

    fun isPrivateIp(ip: String): Boolean {
        val addr = InetAddress.getByName(ip)
        return addr.isSiteLocalAddress || addr.isLoopbackAddress
    }
}
