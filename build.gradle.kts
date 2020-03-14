plugins {
    java
    `java-library`
    `maven-publish`
    id("io.freefair.lombok") version "4.1.6"
    id("org.springframework.boot") version "2.0.9.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
}

group = "cn.wenkang365t"
version = "0.1.0"

java {
    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenLocal()
    mavenCentral()
}

var shiro = "1.5.1"
var jwt = "3.10.0"
var huTool = "5.2.2"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("cn.hutool:hutool-all:${huTool}")
    api("org.apache.shiro:shiro-spring-boot-web-starter:${shiro}")
    api("cn.wenkang365t:jcasbin-extra:0.1.0")
    implementation("com.auth0:java-jwt:${jwt}")
}

tasks.withType<JavaCompile>() {
    options.encoding = "utf-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "utf-8"
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar>() {
    enabled = false
}

tasks.withType<Jar>() {
    enabled = true
}

tasks.withType<Test>() {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("jcasbin extra")
                description.set("JCasbin 的扩充，包含 HutoolDB Adapter，Etcd Watcher")
            }
        }
    }
}
