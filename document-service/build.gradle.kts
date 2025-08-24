dependencies {
    // Core Module
    implementation(project(":core"))
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    // Spring Cloud
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    // PostgreSQL
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    // Redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.50.0")
    // Excel POI
    implementation("org.apache.poi:poi:5.4.1")
    implementation("org.apache.poi:poi-ooxml:5.4.1")
    // Powerpoint POI
    implementation("org.apache.poi:poi-scratchpad:5.4.1")
    implementation("org.apache.poi:poi-ooxml-full:5.4.1")
    // Reactor
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    // Test
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}