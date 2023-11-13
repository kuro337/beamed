import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java

}

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

java {
    toolchain {
        //languageVersion.set(JavaLanguageVersion.of(9))
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

allprojects {


    tasks.withType<Test> {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    plugins.withType<JavaPlugin> {
        extensions.configure<JavaPluginExtension> {
            //sourceCompatibility = JavaVersion.VERSION_1_9
            //  targetCompatibility = JavaVersion.VERSION_1_9
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }
    }

    tasks.withType<KotlinCompile> {


        kotlinOptions {
            //jvmTarget = "9"
            jvmTarget = "11"
        }
    }
}


/* gradle checkBytecodeVersion --console=rich */
tasks.register("checkBytecodeVersion") {
    doLast {
        val classes = listOf(
            "app/build/classes/kotlin/main/eventstream/app/AppKt.class",
            "beam/build/classes/kotlin/main/eventstream/beam/AppKt.class",
            "kafka/build/classes/kotlin/main/eventstream/kafka/AppKt.class"
        )
        for (classPath in classes) {
            println("Checking bytecode version for $classPath")
            exec {
                commandLine("sh", "-c", "javap -verbose $classPath | grep major")
                /* Verbose Output commandLine("javap", "-verbose", classPath) */
                standardOutput = System.out
            }
        }
    }
}

/*
Java Class Version Reference

50 = Java 6
51 = Java 7
52 = Java 8
53 = Java 9
54 = Java 10
55 = Java 11
56 = Java 12
57 = Java 13
58 = Java 14
59 = Java 15
60 = Java 16
61 = Java 17
62 = Java 18
63 = Java 19
64 = Java 20
65 = Java 21
66 = Java 22
 */