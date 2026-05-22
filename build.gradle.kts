// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

/**
 * Formats all Kotlin source files across every subproject:
 * fixes indentation, removes unused imports and applies Kotlin code style.
 *
 * Usage:
 *   ./gradlew codeFormat
 */
tasks.register("codeFormat") {
    group = "formatting"
    description = "Formats all Kotlin files: fixes indentation, removes unused imports and applies code style."

    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("removeUnusedImports")
        } + subprojects.mapNotNull { sub ->
            sub.tasks.findByName("ktlintFormat")
        }
    )
}

/**
 * Checks Kotlin code style + static analysis (unused imports, code smells) across every
 * subproject without modifying files. Useful for CI pipelines.
 *
 * Usage:
 *   ./gradlew codeCheck
 */
tasks.register("codeCheck") {
    group = "formatting"
    description = "Checks code style and static analysis across all subprojects (read-only, fails on violations)."

    dependsOn(
        subprojects.mapNotNull { sub ->
            sub.tasks.findByName("ktlintCheck")
        } + subprojects.mapNotNull { sub ->
            sub.tasks.findByName("detekt")
        }
    )
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
