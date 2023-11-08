plugins {
    id("eventstream.kotlin-application-conventions")
    application
}

dependencies {
    implementation("eventstream:beam:1.0.2")
}

application {
    mainClass.set("eventstream.app.AppKt")
}
