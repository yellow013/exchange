plugins {
    java
}

version = "unspecified"

dependencies {
    testImplementation("org.jmock:jmock-legacy:2.5.1")
    testImplementation("org.mockito:mockito-core:4.2.0")
    implementation("com.google.guava:guava:13.0")
    implementation("org.slf4j:slf4j-api:1.7.16")
    implementation("org.assertj:assertj-core:3.17.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}