package eventstream.kafka.controller

class KafkaControllerLogging {
    companion object {
        fun runtimeClientSecretMissingError(): String {
            return """
                    @EnvironmentError
                    Make Sure the Kafka Client Password is Set in the Environment.
                    
                    @ENV_VAR -> KAFKA_PASSWORD
                    
                    @Kubernetes
                     
                     - Make sure the deployment has the Environment Variable Set.
                     
                     ```yaml
                     env:
                       - name: KAFKA_PASSWORD
                         valueFrom:
                           secretKeyRef:
                             name: kafka-user-passwords
                             key:  client-passwords
                     ```
                    """.trimIndent()
        }
    }
}