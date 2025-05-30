plugins {
    kotlin("jvm") version "2.1.20"
}

group = "com.github.hakobune8"
version = "1.0.4"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}
