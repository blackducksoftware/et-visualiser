import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "com.synopsys.integration"
version = "0.0.1-SNAPSHOT"

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.31")
    implementation("com.google.apis:google-api-services-analyticsreporting:v4-rev20190318-1.28.0")
    implementation("com.google.http-client:google-http-client-gson:1.29.1")
    implementation("com.beust:klaxon:5.0.5")
    implementation("tech.tablesaw:tablesaw-core:0.32.7")
    implementation("tech.tablesaw:tablesaw-jsplot:0.32.7")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val shadowJar: ShadowJar by tasks
shadowJar.apply {
    manifest {
        attributes["Implementation-Title"] = "E.T. Visualizer"
        attributes["Implementation-Version"] = version
        attributes["Main-Class"] = "ApplicationKt"
    }

    baseName = project.name
}