import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    // Kafka
    implementation("org.springframework.kafka:spring-kafka")
    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}