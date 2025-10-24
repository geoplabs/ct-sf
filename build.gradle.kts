import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
    antlr
    id("com.diffplug.spotless") version "6.25.0"
    application
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

// Set the main class for the application
application {
    mainClass.set("com.sustainability.CarbonTrackerApplicationKt")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

val kotlinLoggingVersion = "3.0.5"
val antlrVersion = "4.11.1"
val mockitoVersion = "2.1.0"
val qudtlibVersion = "6.8.0"
val jsonPathVersion = "2.9.0"
val jsonSmartVersion = "2.5.1"
val gsonVersion = "2.11.0"
val springdocOpenapiVersion = "1.8.0"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("io.github.qudtlib:qudtlib:$qudtlibVersion")
    implementation("com.jayway.jsonpath:json-path:$jsonPathVersion")
    implementation("net.minidev:json-smart:$jsonSmartVersion")
    implementation("com.google.code.gson:gson:$gsonVersion")
    implementation("org.springdoc:springdoc-openapi-kotlin:$springdocOpenapiVersion")
    implementation("jakarta.annotation:jakarta.annotation-api")
    compileOnly("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    antlr("org.antlr:antlr4:$antlrVersion")
    antlr("org.antlr:antlr4-runtime:$antlrVersion")

    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.generateGrammarSource {
    outputDirectory = layout.buildDirectory.dir("generated/sources/main/kotlin/antlr").get().asFile
    arguments = arguments + listOf("-package", "com.sustainability") + listOf("-visitor", "-no-listener")
}

// Generate ANTLR sources for test grammar
tasks.generateTestGrammarSource {
    outputDirectory = layout.buildDirectory.dir("generated-src/antlr/test").get().asFile
    arguments = arguments + listOf("-package", "com.sustainability.test", "-visitor", "-no-listener")
}

tasks.withType<KotlinCompile> {
    // Ensure main compilation waits for ANTLR main generation
    dependsOn(tasks.generateGrammarSource)
}

// Fix for test compilation dependency
tasks.named("compileTestKotlin") {
    dependsOn(tasks.generateTestGrammarSource)
}

sourceSets {
    main {
        java {
            // so that the Java files ANTLR generated (Calculations*.java) are compiled
            srcDir("$buildDir/generated/sources/main/kotlin/antlr")
        }
        kotlin {
            // in case you ever generate Kotlin into the same dir
            srcDir("$buildDir/generated/sources/main/kotlin/antlr")
        }
    }
    test {
        java {
            srcDir("$buildDir/generated-src/antlr/test")
        }
        kotlin {
            srcDir("$buildDir/generated-src/antlr/test")
        }
    }
}

spotless {
    format("misc") {
        target("*.md", ".gitignore")
        trimTrailingWhitespace()
        indentWithSpaces()
        endWithNewline()
    }
    kotlin {
        target("**/src/**/*.kt")
        trimTrailingWhitespace()
        endWithNewline()
        ktlint().editorConfigOverride(
            mapOf(
                "ktlint_standard_discouraged-comment-location" to "disabled",
                "ktlint_standard_value-argument-comment" to "disabled",
                "ktlint_standard_value-parameter-comment" to "disabled",
                "max_line_length" to "120",
                "ktlint_ignore_back_ticked_identifier" to true,
            ),
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.gradle.kts")
        trimTrailingWhitespace()
        endWithNewline()
        ktlint()
    }
}
