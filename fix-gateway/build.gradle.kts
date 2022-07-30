import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar


plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

val codecs: Configuration by configurations.creating

configurations {
    implementation {
        isCanBeResolved = true
    }
}

val genDir = "$buildDir/generated-src"
val genDirFile = file(genDir)

sourceSets {
    main {
        java.srcDir(genDirFile)
    }

    test {
        java.srcDir(genDirFile)
    }
}

dependencies {
    codecs(Dependencies.artioCodecs)
    implementation(project(":core"))
    implementation(project(":config"))
    testImplementation(project(":testing"))
    implementation("it.unimi.dsi:fastutil:8.4.0")
    implementation(Dependencies.aeron)
    implementation(Dependencies.aeronic)
    implementation(Dependencies.agrona)
    implementation(Dependencies.artioCore)
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node", "admin-api", "fix-gateway")
        }
    }

    doLast {
        exec {
            commandLine("bash", "${rootDir}/stop-exchange.sh")
        }
    }
}

tasks {
    compileJava {
        dependsOn("generateFIXCodecs")
    }

    clean {
        dependsOn("cleanFIXCodecs")
    }

    register<Delete>("cleanFIXCodecs") {
        delete("$genDir/exchange/lob/api/codecs/fix")
    }

    register<JavaExec>("generateFIXCodecs") {
        dependsOn("cleanFIXCodecs")
        main = "uk.co.real_logic.artio.dictionary.CodecGenerationTool"
        classpath = configurations.getByName("codecs")
        outputs.dir(genDir)
        systemProperties(
                Pair("fix.codecs.flyweight", "true"),
                Pair("fix.codecs.parent_package", "exchange.lob.api.codecs.fix")
        )
        args(genDir, "src/main/resources/session-dictionary.xml")
    }

    register<ShadowJar>("fixGatewayJar") {
        destinationDirectory.set(buildDir)
        archiveFileName.set("fix-gateway.jar")
        manifest {
            attributes["Main-Class"] = "exchange.lob.fix.FixGatewayMain"
        }
        from(sourceSets.main.get().output)
        from(project.configurations.implementation)
    }
}