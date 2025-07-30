dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    // Redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.50.0")
    // khttp
    implementation("org.danilopianini:khttp:1.6.3")
    // MySQL
    runtimeOnly("com.mysql:mysql-connector-j")
    // Test
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.springframework.security:spring-security-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}