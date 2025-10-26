import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
}

tasks.named<BootJar>("bootJar") {
    enabled = false
}