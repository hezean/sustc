import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.spring") version "1.9.21"
    kotlin("plugin.lombok") version "1.9.21"
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.7.3")
}

tasks.withType<BootRun> {
    enabled = false
}

tasks.withType<BootJar> {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xno-param-assertions"
    }
}

tasks.register("submitJar") {
    group = "application"
    description = "Prepare an uber-JAR for submission"

    tasks.getByName<ShadowJar>("shadowJar") {
        archiveFileName = "sustc-api.jar"
        destinationDirectory = File("$rootDir/submit")
        dependencies {
            exclude(dependency("ch.qos.logback:logback-.*"))
        }
    }.let { dependsOn(it) }
}

tasks.clean {
    delete(fileTree("$rootDir/submit").matching { include("*.jar") })
}
