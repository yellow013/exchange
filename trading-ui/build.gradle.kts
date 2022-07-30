import com.github.gradle.node.npm.task.NpxTask

plugins {
    java
    id("com.github.node-gradle.node") version "3.0.0"
}

dependencies {
    implementation("com.codeborne:selenide:5.20.4")
    testImplementation(project(":testing"))
    implementation(Dependencies.artioCore)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    // can add selenide browser property here

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node", "admin-api", "rest-api", "fix-gateway", "trading-ui")
        }
    }

    doLast {
        exec {
            commandLine("bash", "${rootDir}/stop-exchange.sh")
        }
    }
}

tasks {
    val uiSrcPath = "${rootDir}/trading-ui/src/main/js"

    register<NpxTask>("installTradingUI") {
        workingDir.set(File(uiSrcPath))
        command.set("npm")
        args.set(listOf("install"))
    }

    register<Delete>("cleanNodeModules") {
        delete("$uiSrcPath/node_modules")
    }

    clean {
        dependsOn("cleanNodeModules")
    }
}
