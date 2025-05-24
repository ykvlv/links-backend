package dev.ykvlv.links.analytics.application.consumer

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.ykvlv.links.analytics.application.data.dto.ClickEvent
import dev.ykvlv.links.analytics.application.data.repository.ClickhouseEventRepository
import dev.ykvlv.links.analytics.application.service.EnrichmentService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class ClickEventConsumer(
    private val enrichmentService: EnrichmentService,
    private val repository: ClickhouseEventRepository
) {

    private val logger = LoggerFactory.getLogger(ClickEventConsumer::class.java)
    private val mapper = jacksonObjectMapper()

    @KafkaListener(topics = ["\${app.kafka-topic-clicks}"])
    fun listen(record: String) {
        try {
            val event = mapper.readValue(record, ClickEvent::class.java)
            val enriched = enrichmentService.enrich(event)
            repository.save(enriched)
            logger.info("Successfully processed event for slug: {}", event.slug)
        } catch (e: Exception) {
            logger.error("Error processing record. Record: {}. Error: {}", record, e.message, e)
            throw e
        }
    }

}
