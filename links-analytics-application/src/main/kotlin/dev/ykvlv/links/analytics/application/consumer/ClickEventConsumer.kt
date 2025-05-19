package dev.ykvlv.links.analytics.application.consumer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.ykvlv.links.analytics.application.config.AppProperties
import dev.ykvlv.links.analytics.application.data.dto.ClickEvent
import dev.ykvlv.links.analytics.application.service.EnrichmentService
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ClickEventConsumer(
    private val enrichmentService: EnrichmentService,
) {
    @KafkaListener(topics = ["\${app.kafka-topic-clicks}"])
    fun listen(record: String) {
        val mapper = jacksonObjectMapper()
        val event = mapper.readValue(record, ClickEvent::class.java)
        val enriched = enrichmentService.enrich(event)

        // TODO save to Clickhouse
        println("Enriched event: $enriched")
    }
}
