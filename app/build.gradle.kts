plugins {
    java
    application

    id("com.github.johnrengelman.shadow") version "7.0.0"
}

application {
    mainClass.set("fr.rader.imbob.Main")
}

group = "fr.rader"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("io.github.spair:imgui-java-app:1.86.4")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "fr.rader.imbob.Main"
    }

    archiveFileName.set("${rootProject.name}-${project.version}.jar")
}
