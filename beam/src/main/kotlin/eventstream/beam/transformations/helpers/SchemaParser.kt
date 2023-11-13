package eventstream.beam.transformations.helpers


import org.apache.avro.Schema
import shaded.parquet.com.fasterxml.jackson.databind.ObjectMapper
import shaded.parquet.com.fasterxml.jackson.databind.node.ObjectNode

object SchemaParser {
    fun removeNamespaceFromSchema(schema: Schema): Schema {

        val schemaStr = schema.toString().trim()

        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(schemaStr) as ObjectNode

        // Remove 'namespace' field from the root object
        rootNode.remove("namespace")

        // Serialize the modified rootNode to string
        val modifiedSchemaStr = rootNode.toString()

        // parse and return
        return Schema.Parser().parse(modifiedSchemaStr)
    }
}