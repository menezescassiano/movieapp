// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}

/**
 * Runs all unit tests across every subproject.
 *
 * Usage:
 *   ./gradlew testAll
 *   ./gradlew testAll --continue          # keeps going even if a module fails
 */
tasks.register("testAll") {
    group = "verification"
    description = "Runs all unit tests across all subprojects."

    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("testDebugUnitTest")
        }
    )
}
