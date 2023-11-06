package eventstream.beam.models

import org.apache.beam.sdk.schemas.JavaFieldSchema
import org.apache.beam.sdk.schemas.annotations.DefaultSchema
import org.apache.beam.sdk.schemas.annotations.SchemaCreate

@DefaultSchema(JavaFieldSchema::class)
class SimpleSchema {

    var bank: String
    var purchaseAmount: Double

    // No-argument constructor for Avro
    constructor() {
        bank = ""
        purchaseAmount = 0.0
    }

    // Constructor with parameters
    @SchemaCreate
    constructor(bank: String, purchaseAmount: Double) {
        this.bank = bank
        this.purchaseAmount = purchaseAmount
    }

    override fun toString(): String {
        return "Bank: $bank, Amount: $purchaseAmount"
    }
}