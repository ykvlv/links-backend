package dev.ykvlv.links.analytics.application.utils

import dev.ykvlv.links.analytics.application.config.AppProperties
import dev.ykvlv.links.analytics.application.data.dto.IpRange
import org.springframework.stereotype.Component
import java.io.File
import java.util.concurrent.atomic.AtomicReference

@Component
class IpToAsnLookup(
    private val props: AppProperties
) {
    private val rangesRef = AtomicReference(
        loadTsv(props.ip2asnRelPath).sortedBy { it.start }
    )

    /**
     * Looks up the ASN and country information for a given IPv4 address.
     * Returns null if the IP address is not found in the ranges.
     */
    fun lookup(ipString: String): IpRange? {
        val ipLong = ipv4ToLong(ipString) ?: return null
        val ranges = rangesRef.get()
        var l = 0
        var r = ranges.lastIndex
        while (l <= r) {
            val mid = (l + r) ushr 1
            val range = ranges[mid]
            when {
                ipLong < range.start -> r = mid - 1
                ipLong > range.end   -> l = mid + 1
                else                 -> return range
            }
        }
        return null
    }

    /**
     * Refreshes the in-memory database of IP ranges by loading a new TSV file.
     */
    fun refreshDb() {
        val path = props.ip2asnRelPath
        if (!File(path).exists()) {
            println("ip2asn file not found: $path")
            return
        }
        try {
            val newRanges = loadTsv(path).sortedBy { it.start }
            rangesRef.set(newRanges)
        } catch (e: Exception) {
            println("Failed to reload ip2asn: ${e.message}")
        }
    }

    /**
     * Loads a TSV file containing IP ranges and their associated ASN data.
     */
    private fun loadTsv(path: String) = File(path).useLines { seq ->
        seq.filter { it.isNotBlank() && it[0].isDigit() }
            .map { line ->
                val parts = line.split('\t')
                IpRange(
                    parts[0].toLong(),
                    parts[1].toLong(),
                    parts[2].toLong(),
                    parts[3],
                    parts.getOrNull(4) ?: ""
                )
            }.toList()
    }

    /**
     * Converts an IPv4 address in dotted-decimal notation to a long integer.
     * Returns null if the input is not a valid IPv4 address.
     */
    private fun ipv4ToLong(ip: String): Long? {
        val parts = ip.split('.')
        if (parts.size != 4) return null
        return parts.fold(0L) { acc, part ->
            val p = part.toIntOrNull() ?: return null
            if (p !in 0..255) return null
            (acc shl 8) + p
        }
    }

}
