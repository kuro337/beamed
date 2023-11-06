package eventstream.kafka.client


import eventstream.kafka.config.KafkaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic

class KafkaClient(config: KafkaConfig) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val adminClient = AdminClient.create(config.toProperties())

    fun createTopic(topicName: String, partitions: Int, replicationFactor: Short) {
        logger.info { "Creating Topic $topicName with $partitions partitions." }

        try {
            val topics = adminClient.listTopics().names().get()
            if (topics.contains(topicName)) {
                logger.info { "Topic $topicName already exists." }
            } else {
                val newTopic = NewTopic(topicName, partitions, replicationFactor)
                adminClient.createTopics(listOf(newTopic)).all().get()
                logger.info { "Topic $topicName created successfully." }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun close() {
        adminClient.close()
    }
}



