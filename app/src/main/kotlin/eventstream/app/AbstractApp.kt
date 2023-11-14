package eventstream.app


interface Database {
    fun name(): String
}

class Postgres : Database {
    override fun name(): String = "Postgres"
}

class DuckDB : Database {
    override fun name(): String = "DuckDB"
}

interface Producer<out T : Database> {
    fun produce(): T
}

class PostgresProducer : Producer<Postgres> {
    override fun produce(): Postgres = Postgres()
}

class DuckDBProducer : Producer<DuckDB> {
    override fun produce(): DuckDB = DuckDB()
}

fun processDatabase(producer: Producer<Database>) {
    val fruit = producer.produce()
    println("Produced a ${fruit.name()}")
}

interface Table<T : Database> {
    val database: T
    fun tableName(): String
}

class PostgresTable(override val database: Postgres) : Table<Postgres> {
    override fun tableName(): String = "PostgresTable"
}

class DuckDBTable(override val database: DuckDB) : Table<DuckDB> {
    override fun tableName(): String = "DuckDBTable"
}

fun processTable(table: Table<out Database>) {
    println("Table ${table.tableName()} is in database ${table.database.name()}")
}

fun main() {
    val appleProducer = PostgresProducer()
    val bananaProducer = DuckDBProducer()

    processDatabase(appleProducer)
    processDatabase(bananaProducer)

    val postgresTable = PostgresTable(Postgres())
    val duckDBTable = DuckDBTable(DuckDB())

    processTable(postgresTable)
    processTable(duckDBTable)
}
