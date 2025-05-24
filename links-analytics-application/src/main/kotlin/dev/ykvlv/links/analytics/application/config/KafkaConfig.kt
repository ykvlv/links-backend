package dev.ykvlv.links.analytics.application.config

import org.apache.kafka.common.TopicPartition
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaOperations
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig(
    private val props: AppProperties,
) {

    /**
     * Configures a custom error handler for Kafka listeners.
     */
    @Bean
    fun customKafkaErrorHandler(kafkaOperations: KafkaOperations<Any, Any>): DefaultErrorHandler {
        val recoverer = DeadLetterPublishingRecoverer(kafkaOperations) { consumerRecord, _ ->
            if (consumerRecord.partition() >= 0) {
                TopicPartition(props.kafkaTopicClicksDlt, consumerRecord.partition())
            } else {
                TopicPartition(props.kafkaTopicClicksDlt, 0)
            }
        }

        return DefaultErrorHandler(
            recoverer,
            FixedBackOff(FixedBackOff.DEFAULT_INTERVAL, props.kafkaMaxFailures - 1L)
        )
    }

    /**
     * Configures the Kafka listener container factory.
     */
    @Bean
    fun kafkaListenerContainerFactory(
        consumerFactory: ConsumerFactory<Any, Any>,
        customKafkaErrorHandler: DefaultErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<Any, Any> {
        val factory = ConcurrentKafkaListenerContainerFactory<Any, Any>()
        factory.consumerFactory = consumerFactory
        factory.setCommonErrorHandler(customKafkaErrorHandler)
        factory.setConcurrency(props.kafkaListenerConcurrency)
        return factory
    }

}
