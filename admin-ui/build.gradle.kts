import com.github.gradle.node.npm.task.NpxTask

plugins {
    java
    id("com.github.node-gradle.node") version "3.0.0"
}

version = "unspecified"

dependencies {
    implementation("com.codeborne:selenide:5.20.4")
    testImplementation(project(":testing"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
    // can add selenide browser property here

    doFirst {
        exec {
            commandLine("bash", "${rootDir}/start-exchange.sh", "exchange-node", "admin-api", "rest-api", "fix-gateway", "admin-ui")
        }
    }

    doLast {
        exec {
            commandLine("bash", "${rootDir}/stop-exchange.sh")
        }
    }
}



tasks {
    val uiSrcPath = "${rootDir}/admin-ui/src/main/js"

    register<NpxTask>("installAdminUI") {
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
