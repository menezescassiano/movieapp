plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

detekt {
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
}

// Dedicated detekt task used by removeUnusedImports:
// never fails (ignoreFailures = true), just generates the XML report.
tasks.register<io.gitlab.arturbosch.detekt.Detekt>("detektScan") {
    description = "Scans for unused imports and generates XML report (used by removeUnusedImports)."
    group = "formatting"
    config.setFrom("$rootDir/detekt.yml")
    buildUponDefaultConfig = true
    ignoreFailures = true
    setSource(files("src/main/java", "src/main/kotlin"))
    include("**/*.kt")
    reports {
        xml.required.set(true)
        xml.outputLocation.set(
            layout.buildDirectory.file("reports/detekt/detekt-scan.xml"),
        )
        html.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
    }
}

// Reads the detekt XML report and physically removes every line flagged as
// UnusedImports. The detektScan task is invoked inline via exec() so the
// XML is guaranteed to be written before we parse it.
tasks.register("removeUnusedImports") {
    description = "Removes unused import lines detected by detekt from every Kotlin source file."
    group = "formatting"

    doLast {
        // 1. Run detektScan synchronously via ProcessBuilder to produce a fresh XML report.
        val gradlew = file("$rootDir/gradlew").absolutePath
        val process =
            ProcessBuilder(gradlew, ":app:detektScan", "--rerun-tasks")
                .directory(rootDir)
                .redirectErrorStream(true)
                .start()
        process.inputStream.bufferedReader().forEachLine { /* suppress detektScan output */ }
        val exitCode = process.waitFor()
        if (exitCode != 0) return@doLast

        // 2. Parse the XML and collect lines to remove per file.
        val reportFile = file("${layout.buildDirectory.get().asFile}/reports/detekt/detekt-scan.xml")
        if (!reportFile.exists()) return@doLast

        val factory =
            javax.xml.parsers.DocumentBuilderFactory
                .newInstance()
        val doc = factory.newDocumentBuilder().parse(reportFile)
        val fileNodes = doc.getElementsByTagName("file")

        var totalRemoved = 0

        for (i in 0 until fileNodes.length) {
            val fileNode = fileNodes.item(i) as org.w3c.dom.Element
            val path = fileNode.getAttribute("name")
            val sourceFile = file(path)
            if (!sourceFile.exists()) continue

            val errorNodes = fileNode.getElementsByTagName("error")
            val unusedImportLines = mutableSetOf<Int>()

            for (j in 0 until errorNodes.length) {
                val error = errorNodes.item(j) as org.w3c.dom.Element
                if (error.getAttribute("source") == "detekt.UnusedImports") {
                    error.getAttribute("line").toIntOrNull()?.let { unusedImportLines += it }
                }
            }

            if (unusedImportLines.isEmpty()) continue

            // 3. Rewrite the file without the flagged lines.
            val filteredLines =
                sourceFile
                    .readLines()
                    .filterIndexed { index, _ -> (index + 1) !in unusedImportLines }
            sourceFile.writeText(filteredLines.joinToString("\n") + "\n")

            totalRemoved += unusedImportLines.size
        }

        if (totalRemoved > 0) {
            logger.lifecycle("removeUnusedImports: removed $totalRemoved unused import(s).")
        }

        // 4. Remove duplicate imports (same line appearing more than once in the same file).
        var duplicatesRemoved = 0
        fileTree("src").matching { include("**/*.kt") }.forEach { sourceFile ->
            val lines = sourceFile.readLines()
            val seenImports = mutableSetOf<String>()
            val deduped =
                lines.filter { line ->
                    if (line.startsWith("import ")) seenImports.add(line) else true
                }
            if (deduped.size < lines.size) {
                sourceFile.writeText(deduped.joinToString("\n") + "\n")
                duplicatesRemoved += lines.size - deduped.size
            }
        }

        if (duplicatesRemoved > 0) {
            logger.lifecycle("removeUnusedImports: removed $duplicatesRemoved duplicate import(s).")
        }
    }
}

ktlint {
    version = "1.8.0"
    android = true
    outputToConsole = true
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

android {

    namespace = "com.example.movieapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.movieapp"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "BASE_URL", "\"http://192.168.0.212:8080/\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            buildConfigField("String", "BASE_URL", "\"https://your-production-url.com/\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.coil.compose)
    implementation(libs.androidx.serialization)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.mlkit.vision)

    // Hilt with KSP
    implementation(libs.hilt.android)
    implementation(libs.play.services.mlkit.barcode.scanning)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // YouTube Player
    implementation(libs.youtube.player)

    // Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
