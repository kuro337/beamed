package eventstream.grpc.entities

data class Foo(
    val name: String,
    val age: Int,
    val gender: String,
    val income: Double,
    val hobbies: List<String>

) {
    private var _embedding: List<Float>? = null

    var embedding: List<Float>?
        get() = _embedding
        set(value) {
            _embedding = value
        }

    override fun toString(): String {
        val hobbiesStr = if (hobbies.isNotEmpty()) {
            hobbies.joinToString(", ", prefix = "Hobbies include ", postfix = ".")
        } else {
            "No hobbies listed."
        }
        return "Name is $name, Age is $age, Gender is $gender, Income is $income. $hobbiesStr"
    }
}