package dev.ykvlv.links.analytics.application.scheduler

import dev.ykvlv.links.analytics.application.utils.IpToAsnDataLoader
import dev.ykvlv.links.analytics.application.utils.IpToAsnLookup
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Task that refreshes the on‑disk ip2asn file
 * and atomically replaces the in‑memory snapshot.
 */
@Component
@ConditionalOnProperty(name = ["app.ip2asn-auto-update"], havingValue = "true")
class IpToAsnUpdater(
    private val dataLoader: IpToAsnDataLoader,
    private val lookup: IpToAsnLookup
) {

    @Scheduled(cron = "0 0 3 * * *")
    fun updateIpDb() {
        try {
            dataLoader.downloadAndExtract()
            lookup.refreshDb()
        } catch (e: Exception) {
            println("Failed to update ip2asn: ${e.message}")
        }
    }
}
