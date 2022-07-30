import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "exchange.lob"
version = "1.0-SNAPSHOT"


configurations {
    implementation {
        isCanBeResolved = true
    }
}

val aeron: String by project

dependencies {
    annotationProcessor(Dependencies.aeronic)
    implementation(project(":core"))
    implementation(project(":config"))
    implementation(Dependencies.aeron)
    implementation(Dependencies.aeronic)
    implementation(Dependencies.awaitility)
    testImplementation(project(":testing"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation("com.sparkjava:spark-core:2.9.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node", "admin-api")
        }
    }

    doLast {
        exec {
            commandLine("bash", "${rootDir}/stop-exchange.sh")
        }
    }
}

tasks {
    register<ShadowJar>("adminApiJar") {
        destinationDirectory.set(buildDir)
        archiveFileName.set("admin-api.jar")
        manifest {
            attributes["Main-Class"] = "exchange.lob.admin.AdminApiMain"
        }
        from(sourceSets.main.get().output)
        from(project.configurations.implementation)
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    }
}