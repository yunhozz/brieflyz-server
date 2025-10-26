dependencies {
    // Core Module
    implementation(project(":core"))
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // Spring AI
    implementation("org.springframework.ai:spring-ai-starter-model-ollama")
    implementation("org.springframework.ai:spring-ai-starter-model-openai")
    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    // Redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.50.0")
    // Reactor
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    // Test
    testImplementation("io.projectreactor:reactor-test")
}

extra["springAiVersion"] = "1.0.0"

dependencyManagement {
    imports {
        mavenBom("org.springframework.ai:spring-ai-bom:${property("springAiVersion")}")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}