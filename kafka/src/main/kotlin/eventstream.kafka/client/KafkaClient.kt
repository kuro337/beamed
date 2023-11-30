package eventstream.kafka.client


import eventstream.kafka.config.KafkaConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.ConsumerGroupDescription
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.common.TopicPartition

class KafkaClient(config: KafkaConfig) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val adminClient = AdminClient.create(config.toProperties())


    fun getTopics(): Set<String> {
        return try {
            adminClient.listTopics().names().get()
        } catch (e: Exception) {
            logger.error(e) { "Error while listing topics." }
            emptySet()
        }
    }

    fun getTopicInformation(topicName: String): String {
        val topicDescription = adminClient.describeTopics(listOf(topicName)).allTopicNames().get()
        return topicDescription[topicName].toString()
    }

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

    fun deleteTopic(topicName: String) {
        try {
            logger.info { "Deleting Topic: $topicName" }
            val result = adminClient.deleteTopics(listOf(topicName))
            result.all().get() // Wait for the deletion to complete
            logger.info { "Topic $topicName deleted successfully." }
        } catch (e: Exception) {
            logger.error(e) { "Error while deleting topic: $topicName." }
        }
    }

    fun close() {
        adminClient.close()
    }

    fun listConsumerGroups(): List<String> {

        return adminClient.listConsumerGroups().all().get().map { it.groupId() }
    }

    fun describeConsumerGroup(groupId: String): ConsumerGroupDescription {
        val description = adminClient.describeConsumerGroups(listOf(groupId)).all().get()
        return description[groupId] ?: throw IllegalArgumentException("Consumer group not found: $groupId")
    }

    fun getConsumerLag(groupId: String): Map<TopicPartition, Long> {
        val consumerGroupDescription = adminClient.describeConsumerGroups(listOf(groupId)).all().get()[groupId]
            ?: throw IllegalArgumentException("Consumer group not found: $groupId")

        // Fetching the committed offsets for the group
        val committedOffsets = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get()

        // Preparing OffsetSpec.latest() for each partition
        val offsetSpecMap = committedOffsets.keys.associateWith { OffsetSpec.latest() }

        // Getting the last offsets (end offsets) for each partition the group is consuming
        val endOffsets = adminClient.listOffsets(offsetSpecMap).all().get()

        return committedOffsets.keys.associateWith { topicPartition ->
            val committedOffset = committedOffsets[topicPartition]?.offset() ?: 0L
            val endOffset = endOffsets[topicPartition]?.offset() ?: 0L
            endOffset - committedOffset // Calculating lag
        }
    }


}



