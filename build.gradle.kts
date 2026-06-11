plugins {
    id("com.factory.spring-application-conventions")
    id("com.factory.maven-consumer-conventions")
}

version = "1.0.0"

dependencies {
    // common modules
    implementation("com.factory.common:contract:1.0.1")
    implementation("com.factory.common:kafka:1.0.7")       // CommonKafkaConsumer / EventDispatcher / EventHandler
    implementation("com.factory.common:inbox-jpa:1.0.7")   // @InboxProcessed

    // platform BOM
    implementation(platform("com.factory:platform:1.0.5"))
    annotationProcessor(platform("com.factory:platform:1.0.5"))
    testAnnotationProcessor(platform("com.factory:platform:1.0.5"))

    // Spring Web
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Data JPA
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // QueryDSL
    implementation("io.github.openfeign.querydsl:querydsl-jpa")
    annotationProcessor("io.github.openfeign.querydsl:querydsl-apt") {
        artifact {
            classifier = "jpa"
        }
    }
    annotationProcessor("jakarta.persistence:jakarta.persistence-api")
    testAnnotationProcessor("io.github.openfeign.querydsl:querydsl-apt") {
        artifact {
            classifier = "jpa"
        }
    }
    testAnnotationProcessor("jakarta.persistence:jakarta.persistence-api")

    // Spring AOP (for @InboxProcessed)
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Spring Boot Actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // MariaDB Driver
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.3.3")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok-mapstruct-binding")

    // MapStruct
    implementation("org.mapstruct:mapstruct")
    annotationProcessor("org.mapstruct:mapstruct-processor")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")
    testImplementation("com.factory.common:contract:1.0.1")

    // Dotenv
    implementation("me.paulschwarz:spring-dotenv:4.0.0")

}

tasks.named<Test>("test") {
    useJUnitPlatform()
}