rootProject.name = "sustc"

include(
    "sustc-api",
    "sustc-runner",
)

dependencyResolutionManagement {
    repositories {
        maven("https://maven.aliyun.com/repository/public")
        mavenCentral()
    }
}
