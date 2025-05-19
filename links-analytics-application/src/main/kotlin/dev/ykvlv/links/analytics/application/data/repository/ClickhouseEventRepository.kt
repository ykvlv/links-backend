package dev.ykvlv.links.analytics.application.data.repository

import dev.ykvlv.links.analytics.application.data.dto.EnrichedClickEvent
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Repository
class ClickhouseEventRepository(
    private val jdbcTemplate: JdbcTemplate
) {
    companion object {
        private val chTimestampFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS").withZone(ZoneId.of("UTC"))
    }

    fun save(event: EnrichedClickEvent) {
        jdbcTemplate.update("""
            INSERT INTO enriched_click_event (
                timestamp, event_date, event_hour, event_dow, slug, resolved_url, ip,
                country_code, country_name, city_name, asn, asn_org, browser, os,
                device_type, device_brand, device_name, accept_language, referer,
                referer_host, referer_path, origin, host, is_cache_hit,
                client_fingerprint, is_private_ip
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent(),
            chTimestampFormatter.format(Instant.parse(event.timestamp)),
            event.eventDate,
            event.eventHour.toInt(),
            event.eventDow.toInt(),
            event.slug,
            event.resolvedUrl,
            event.ip,
            event.countryCode,
            event.countryName,
            event.cityName,
            event.asn,
            event.asnOrg,
            event.browser,
            event.os,
            event.deviceType,
            event.deviceBrand,
            event.deviceName,
            event.acceptLanguage,
            event.referer,
            event.refererHost,
            event.refererPath,
            event.origin,
            event.host,
            if (event.isCacheHit) 1 else 0,
            event.clientFingerprint,
            if (event.isPrivateIp) 1 else 0
        )
    }
}
