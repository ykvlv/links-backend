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
        val ua = uaParser.parse(event.user_agent)
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
        val fpInput = "${event.ip}|${event.user_agent}"
        val clientFingerprint = digest.digest(fpInput.toByteArray()).joinToString("") { "%02x".format(it) }
        val isPrivate = isPrivateIp(event.ip)

        return EnrichedClickEvent(
            timestamp = event.timestamp,
            event_date = eventDate,
            event_hour = eventHour,
            event_dow = eventDow,
            slug = event.slug,
            resolved_url = event.resolved_url,
            ip = event.ip,
            country_code = countryCode,
            country_name = countryName,
            city_name = null,
            asn = asn,
            asn_org = asnOrg,
            browser = browser,
            os = os,
            device_type = deviceType,
            device_brand = deviceBrand,
            device_name = deviceName,
            accept_language = event.accept_language,
            referer = event.referer,
            referer_host = refererHost,
            referer_path = refererPath,
            origin = event.origin,
            host = event.host,
            is_cache_hit = event.is_cache_hit,
            client_fingerprint = clientFingerprint,
            is_private_ip = isPrivate
        )
    }

    fun isPrivateIp(ip: String): Boolean {
        val addr = InetAddress.getByName(ip)
        return addr.isSiteLocalAddress || addr.isLoopbackAddress
    }
}
