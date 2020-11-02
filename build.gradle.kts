plugins {
    java
}

group = "org.example"
version = "1.0-SNAPSHOT"

val jUnitVersion = "5.7.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter", "junit-jupiter", jUnitVersion)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
