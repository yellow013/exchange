plugins {
    java
}

dependencies {
    implementation(project(":core"))
    implementation(project(":admin-api"))
    implementation(project(":fix-gateway"))
    implementation(project(":market-data"))
    implementation(project(":testing:fix"))
    implementation("org.apache.commons:commons-lang3:3.10")
    implementation("com.codeborne:selenide:5.20.4")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
    implementation(Dependencies.artioCore)
    implementation(Dependencies.aeron)
    implementation(Dependencies.aeronic)
    implementation(Dependencies.agrona)
    implementation(Dependencies.awaitility)
    implementation("com.google.guava:guava:29.0-jre")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation("com.lmax:simple-dsl:2.2")
    implementation("org.assertj:assertj-core:3.17.2")
    implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}