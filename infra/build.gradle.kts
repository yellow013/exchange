plugins {
    java
}

dependencies {
    implementation(project(":core"))
    implementation(Dependencies.aeron)
    implementation(Dependencies.aeronic)
    implementation(Dependencies.awaitility)
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}