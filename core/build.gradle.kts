dependencies {
    implementation("org.springframework.boot:spring-boot-starter-log4j2")
}

configurations.forEach {
    it.exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    it.exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    it.exclude(group = "ch.qos.logback", module = "logback-classic")
}