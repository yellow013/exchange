import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "exchange.lob"
version = "1.0-SNAPSHOT"


val codecs: Configuration by configurations.creating

configurations {
    implementation {
        isCanBeResolved = true
    }
}

val genDir = "$buildDir/generated-src"
val genDirFile = file(genDir)

dependencies {
    codecs("uk.co.real-logic:sbe-tool:1.20.3")
    annotationProcessor(Dependencies.aeronic)
    implementation(Dependencies.aeronic)
    testImplementation("org.assertj:assertj-core:3.17.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.5.2")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation(files(genDir))
    implementation(project(":config"))
    testImplementation(project(":testing"))
    implementation("com.lmax:simple-dsl:2.2")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("it.unimi.dsi:fastutil:8.4.0")
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation(Dependencies.aeron)
    implementation(Dependencies.agrona)
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2")
    implementation(files(genDir))
}

sourceSets {
    main {
        java.srcDir(genDirFile)
    }

    test {
        java.srcDir(genDirFile)
    }
}

val validationXsdPath = "$projectDir/src/main/resources/sbe/sbe.xsd"


tasks.getByName<Test>("test") {
    useJUnitPlatform()

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node")
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
        dependsOn("generateInternalCodecs")
    }

    clean {
        dependsOn("cleanInternalCodecs")
    }

    register<Delete>("cleanInternalCodecs") {
        delete("$genDir/exchange/lob/api/codecs/internal")
    }

    register<JavaExec>("generateInternalCodecs") {
        dependsOn("cleanInternalCodecs")
        main = "uk.co.real_logic.sbe.SbeTool"
        classpath = configurations.getByName("codecs")
        systemProperties(
            Pair("sbe.output.dir", genDirFile),
            Pair("sbe.target.language", "Java"),
            Pair("sbe.java.generate.interfaces", "true"),
            Pair("sbe.validation.stop.on.error", "true"),
            Pair("sbe.xinclude.aware", "true"),
            Pair("sbe.validation.xsd", validationXsdPath),
            Pair("sbe.target.namespace", "exchange.lob.api.sbe")
        )
        args("src/main/resources/sbe/snapshot.xml")
    }

    register<ShadowJar>("exchangeNodeJar") {
        destinationDirectory.set(buildDir)
        archiveFileName.set("exchange-node.jar")
        manifest {
            attributes["Main-Class"] = "exchange.lob.node.ExchangeNodeMain"
        }
        from(sourceSets.main.get().output)
        from(project.configurations.implementation)
    }
}

