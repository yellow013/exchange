plugins {
    java
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(Dependencies.apacheBeanUtils)
    implementation(Dependencies.apacheConfiguration)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}