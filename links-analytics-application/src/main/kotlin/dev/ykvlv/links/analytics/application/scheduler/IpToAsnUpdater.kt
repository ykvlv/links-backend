package dev.ykvlv.links.analytics.application.scheduler

import dev.ykvlv.links.analytics.application.config.AppProperties
import dev.ykvlv.links.analytics.application.utils.IpToAsnLookup
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.util.zip.GZIPInputStream

@Component
@ConditionalOnProperty(name = ["app.ip2asn-auto-update"], havingValue = "true", matchIfMissing = true)
class IpToAsnUpdater(
    private val lookup: IpToAsnLookup,
    private val props: AppProperties
) {

    /**
     * Scheduled task to update the ip2asn database from a remote URL.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    fun updateIpDb() {
        try {
            val url = URI.create(props.ip2asnUrl).toURL()
            val gzFile = File(props.ip2asnRelPath + ".gz")
            val tsvFile = File(props.ip2asnRelPath)
            println("Updating ip2asn from $url...")

            url.openStream().use { input ->
                gzFile.outputStream().use { output -> input.copyTo(output) }
            }
            println("ip2asn downloaded: ${gzFile.length()} bytes")

            GZIPInputStream(gzFile.inputStream()).use { input ->
                tsvFile.outputStream().use { output -> input.copyTo(output) }
            }
            println("ip2asn extracted: ${tsvFile.length()} bytes")

            lookup.refreshDb()
        } catch (e: Exception) {
            println("Failed to update ip2asn: ${e.message}")
        }
    }

}
