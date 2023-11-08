plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("eventstream.kotlin-common-conventions")
    application
}


repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}


