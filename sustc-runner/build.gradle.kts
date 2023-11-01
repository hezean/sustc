import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    java
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependencyManagement)
    alias(libs.plugins.lombok)
}

dependencies {
    implementation(
        fileTree("$rootDir/submit").matching { include("*.jar") }
            .takeIf { !it.isEmpty } ?: project(":sustc-api")
    )
    runtimeOnly("org.postgresql:postgresql")

    implementation(platform("org.springframework.shell:spring-shell-dependencies:2.1.13"))
    implementation("org.springframework.shell:spring-shell-starter")

    implementation("com.opencsv:opencsv:5.8")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

tasks.withType<BootRun> {
    standardInput = System.`in`
    jvmArgs("-Dbenchmark.data-path=$projectDir/data")
}

tasks.register("benchmark") {
    group = "application"
    description = "Run the benchmark script"

    tasks.getByName<BootRun>("bootRun")
        .apply { args("--spring.profiles.active=benchmark") }
        .let { finalizedBy(it) }
}

tasks.withType<BootJar> {
    archiveFileName = "sustc-runner.jar"
    destinationDirectory = File("$rootDir/run")
}
