package dev.ykvlv.links.analytics.application.utils

import dev.ykvlv.links.analytics.application.data.dto.IpRange
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

/**
 * Thread‑safe, in‑memory IP->ASN lookup table.
 * The backing list is replaced atomically whenever new data is loaded.
 */
@Component
class IpToAsnLookup(
    private val dataLoader: IpToAsnDataLoader
) {

    /** Guards readers during a live refresh. */
    private val rangesRef = AtomicReference<List<IpRange>>()

    @PostConstruct
    fun init() {
        rangesRef.set(dataLoader.loadRanges())
    }

    /**
     * Binary‑searches the ranges for the given IPv4 address.
     * Returns `null` when not found or when the input is malformed.
     */
    fun lookup(ipString: String): IpRange? {
        val ipLong = ipv4ToLong(ipString) ?: return null
        val ranges = rangesRef.get()

        var l = 0
        var r = ranges.lastIndex
        while (l <= r) {
            val mid   = (l + r) ushr 1
            val range = ranges[mid]
            when {
                ipLong < range.start -> r = mid - 1
                ipLong > range.end   -> l = mid + 1
                else                 -> return range
            }
        }
        return null
    }

    /** Hot‑swaps the current snapshot with a freshly parsed one. */
    fun refreshDb() {
        rangesRef.set(dataLoader.loadRanges())
    }

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
