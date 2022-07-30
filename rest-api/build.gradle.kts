import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

configurations {
    implementation {
        isCanBeResolved = true
    }
}

dependencies {
    implementation(Dependencies.agrona)
    implementation(Dependencies.aeron)
    implementation(Dependencies.aeronic)
    implementation(Dependencies.artioCore)
    implementation(Dependencies.awaitility)
    implementation(project(":config"))
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("io.vertx:vertx-web-openapi:4.1.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation(project(":core"))
    testImplementation(project(":testing"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node", "admin-api", "rest-api", "fix-gateway")
        }
    }

    doLast {
        exec {
            commandLine("bash", "${rootDir}/stop-exchange.sh")
        }
    }
}

tasks {
    register<ShadowJar>("restApiJar") {
        destinationDirectory.set(buildDir)
        archiveFileName.set("rest-api.jar")
        manifest {
            attributes["Main-Class"] = "exchange.lob.rest.RestApiMain"
        }
        from(sourceSets.main.get().output)
        from(project.configurations.implementation)
    }
}