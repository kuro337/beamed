# Covariance

- If we have a Companion Object - and want to use it's static Methods to product SubTypes of the Parent Type

```kotlin
package eventstream.app

// Define a Database interface
interface Database {
    fun name(): String
}

// Postgres implements Database interface
class Postgres : Database {
    override fun name(): String = "Postgres"
}

// DuckDB implements Database interface
class DuckDB : Database {
    override fun name(): String = "DuckDB"
}

// Producer interface with out-projection to produce fruits
interface Producer<out T : Database> {
    fun produce(): T
}

// PostgresProducer to produce Postgres instances
class PostgresProducer : Producer<Postgres> {
    override fun produce(): Postgres = Postgres()
}

// DuckDBProducer to produce DuckDB instances
class DuckDBProducer : Producer<DuckDB> {
    override fun produce(): DuckDB = DuckDB()
}

// Function to process any DB producer
fun processDatabase(producer: Producer<Database>) {
    val fruit = producer.produce()
    println("Produced a ${fruit.name()}")
}

// Define a Table interface with a generic type parameter
interface Table<T : Database> {
    val database: T
    fun tableName(): String
}

// Concrete implementation of Table for Postgres
class PostgresTable(override val database: Postgres) : Table<Postgres> {
    override fun tableName(): String = "PostgresTable"
}

// Concrete implementation of Table for DuckDB
class DuckDBTable(override val database: DuckDB) : Table<DuckDB> {
    override fun tableName(): String = "DuckDBTable"
}

// Function to process any table of type Database
fun processTable(table: Table<out Database>) {
    println("Table ${table.tableName()} is in database ${table.database.name()}")
}

fun main() {
    val appleProducer = PostgresProducer()
    val bananaProducer = DuckDBProducer()


    processDatabase(appleProducer)  // Output: Produced a Postgres
    processDatabase(bananaProducer) // Output: Produced a DuckDB

    val postgresTable = PostgresTable(Postgres())
    val duckDBTable = DuckDBTable(DuckDB())

    processTable(postgresTable) // Output: Table PostgresTable is in database Postgres
    processTable(duckDBTable)  // Output: Table DuckDBTable is in database DuckDB
}
```