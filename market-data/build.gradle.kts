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
    implementation(project(":core"))
    implementation(project(":config"))
    testImplementation(project(":testing"))
    implementation(Dependencies.aeron)
    implementation(Dependencies.aeronic)
    implementation(Dependencies.artioCore)
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation(Dependencies.awaitility)
    implementation("io.vertx:vertx-core:4.1.0")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node", "fix-gateway", "admin-api", "market-data")
        }
    }

    doLast {
        exec {
            commandLine("bash", "${rootDir}/stop-exchange.sh")
        }
    }
}

tasks {
    register<ShadowJar>("marketDataServerJar") {
        destinationDirectory.set(buildDir)
        archiveFileName.set("market-data.jar")
        manifest {
            attributes["Main-Class"] = "exchange.lob.md.MarketDataWebSocketServerMain"
        }
        from(sourceSets.main.get().output)
        from(project.configurations.implementation)
    }
}