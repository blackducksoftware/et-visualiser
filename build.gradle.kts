import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
}

group = "com.synopsys.integration"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("no.tornado:tornadofx:1.7.17")
    implementation("com.google.apis:google-api-services-analyticsreporting:v4-rev20190318-1.28.0")
    implementation("com.google.http-client:google-http-client-gson:1.29.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
} 