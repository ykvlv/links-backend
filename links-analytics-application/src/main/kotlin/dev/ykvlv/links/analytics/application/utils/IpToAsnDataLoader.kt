package dev.ykvlv.links.analytics.application.utils

import dev.ykvlv.links.analytics.application.config.AppProperties
import dev.ykvlv.links.analytics.application.data.dto.IpRange
import org.springframework.stereotype.Component
import java.io.File
import java.net.URI
import java.util.zip.GZIPInputStream

/**
 * Handles downloading, extracting and parsing the ip2asn TSV file.
 */
@Component
class IpToAsnDataLoader(
    private val props: AppProperties
) {

    /**
     * Makes sure the TSV file exists locally (download + extract)
     * and returns the resulting `File` reference.
     */
    fun ensureLocalCopy(): File {
        val tsvFile = File(props.ip2asnRelPath)
        if (!tsvFile.exists()) {
            downloadAndExtract()
        }
        return tsvFile
    }

    /**
     * Downloads the gzipped TSV from the configured URL and extracts it
     * to `props.ip2asnRelPath`.
     */
    fun downloadAndExtract() {
        val url     = URI.create(props.ip2asnUrl).toURL()
        val gzFile  = File(props.ip2asnRelPath + ".gz")
        val tsvFile = File(props.ip2asnRelPath)

        gzFile.parentFile?.mkdirs()
        tsvFile.parentFile?.mkdirs()

        println("Downloading ip2asn from $url …")
        url.openStream().use { input ->
            gzFile.outputStream().use { output -> input.copyTo(output) }
        }
        println("ip2asn downloaded: ${gzFile.length()} bytes")

        GZIPInputStream(gzFile.inputStream()).use { input ->
            tsvFile.outputStream().use { output -> input.copyTo(output) }
        }
        println("ip2asn extracted: ${tsvFile.length()} bytes")
    }

    /**
     * Parses the TSV into a list of `IpRange`, always sorted by `start`.
     */
    fun loadRanges(): List<IpRange> =
        ensureLocalCopy().useLines { seq ->
            seq.filter { it.isNotBlank() && it[0].isDigit() }
                .map { line ->
                    val p = line.split('\t')
                    IpRange(
                        p[0].toLong(),
                        p[1].toLong(),
                        p[2].toLong(),
                        p[3],
                        p.getOrNull(4) ?: ""
                    )
                }
                .sortedBy { it.start }
                .toList()
        }

}
