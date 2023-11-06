/**
 * Precompiled [eventstream.kotlin-application-conventions.gradle.kts][Eventstream_kotlin_application_conventions_gradle] script plugin.
 *
 * @see Eventstream_kotlin_application_conventions_gradle
 */
public
class Eventstream_kotlinApplicationConventionsPlugin : org.gradle.api.Plugin<org.gradle.api.Project> {
    override fun apply(target: org.gradle.api.Project) {
        try {
            Class
                .forName("Eventstream_kotlin_application_conventions_gradle")
                .getDeclaredConstructor(org.gradle.api.Project::class.java, org.gradle.api.Project::class.java)
                .newInstance(target, target)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            throw e.targetException
        }
    }
}
