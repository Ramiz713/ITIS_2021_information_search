import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}

group = "me.nebor"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/cuba-platform/main/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
    implementation("org.jsoup:jsoup:1.13.1")

    implementation("com.google.code.gson:gson:2.8.6")
//    implementation("org.apache.lucene:lucene-core:8.8.1")
    implementation("org.apache.lucene.morphology:english:1.1")
    implementation("org.apache.lucene.morphology:russian:1.1")
    implementation("org.apache.lucene.morphology:morph:1.1")
    implementation("com.bpodgursky:jbool_expressions:1.23")

    testImplementation(kotlin("test-junit"))
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = "MainKt"
}