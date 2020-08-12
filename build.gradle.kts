import org.gradle.api.tasks.testing.logging.TestLogEvent.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
  kotlin("jvm") version "1.3.72"
  application
  id("org.jlleitschuh.gradle.ktlint") version "9.3.0" apply true
}

group = "io.simplematter"
version = "1.0.0-SNAPSHOT"

repositories {
  mavenCentral()
  jcenter()
}

val kotlinVersion = "1.3.72"
val vertxVersion = "3.9.2"
val junitJupiterVersion = "5.6.0"
val log4j2Version = "2.11.0"

val mainVerticleName = "io.simplematter.smartapartment.MainVerticle"
val watchForChange = "src/**/*"
val doOnChange = "./gradlew classes"
val launcherClassName = "io.simplematter.smartapartment.Service"

application {
  mainClassName = launcherClassName
}

dependencies {
  implementation("io.vertx:vertx-lang-kotlin:$vertxVersion")
  implementation("io.vertx:vertx-mqtt:$vertxVersion")
  implementation("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
  implementation(kotlin("stdlib-jdk8"))
  implementation("org.apache.logging.log4j:log4j-api:$log4j2Version")
  implementation("org.apache.logging.log4j:log4j-core:$log4j2Version")
  implementation("org.apache.logging.log4j:log4j-slf4j-impl:$log4j2Version")

  // Config
  implementation("io.github.config4k:config4k:0.4.1")
  implementation("com.typesafe:config:1.4.0")

  //JSON
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.11.2")

  // Test
  testImplementation("io.vertx:vertx-junit5:$vertxVersion")
  testImplementation("org.junit.jupiter:junit-jupiter:$junitJupiterVersion")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions.jvmTarget = "1.8"

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    events = setOf(PASSED, SKIPPED, FAILED)
  }
}

ktlint {
  verbose.set(true)
  outputToConsole.set(true)
  reporters {
    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
  }
  ignoreFailures.set(true)
}
