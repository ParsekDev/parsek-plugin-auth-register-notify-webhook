plugins {
    kotlin("jvm") version "1.9.20"
    kotlin("kapt") version "1.9.20"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    `maven-publish`
}

group = "co.statu.rule"
version =
    (if (project.hasProperty("version") && project.findProperty("version") != "unspecified") project.findProperty("version") else "local-build")!!

val pf4jVersion: String by project
val vertxVersion: String by project
val springContextVersion: String by project

val bootstrap = (project.findProperty("bootstrap") as String?)?.toBoolean() ?: false
val pluginsDir: File? by rootProject.extra

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    if (bootstrap) {
        compileOnly(project(mapOf("path" to ":Parsek")))
        compileOnly(project(mapOf("path" to ":plugins:parsek-plugin-auth")))
        compileOnly(project(mapOf("path" to ":plugins:parsek-plugin-database")))
    } else {
        compileOnly("com.github.StatuParsek:Parsek:main-SNAPSHOT")
        compileOnly("com.github.StatuParsek:parsek-plugin-auth:v2.4.2")
        compileOnly("com.github.StatuParsek:parsek-plugin-database:v1.2.1")
    }

    compileOnly(kotlin("stdlib-jdk8"))

    compileOnly("org.pf4j:pf4j:${pf4jVersion}")
    kapt("org.pf4j:pf4j:${pf4jVersion}")

    compileOnly("io.vertx:vertx-web:$vertxVersion")
    compileOnly("io.vertx:vertx-lang-kotlin:$vertxVersion")
    compileOnly("io.vertx:vertx-lang-kotlin-coroutines:$vertxVersion")
    compileOnly("io.vertx:vertx-jdbc-client:$vertxVersion")
    compileOnly("io.vertx:vertx-json-schema:$vertxVersion")
    compileOnly("io.vertx:vertx-web-client:$vertxVersion")
    compileOnly("io.vertx:vertx-web-client:$vertxVersion")

    // https://mvnrepository.com/artifact/org.springframework/spring-context
    compileOnly("org.springframework:spring-context:$springContextVersion")
}

tasks {
    shadowJar {
        val pluginId: String by project
        val pluginClass: String by project
        val pluginProvider: String by project
        val pluginDependencies: String by project

        manifest {
            attributes["Plugin-Class"] = pluginClass
            attributes["Plugin-Id"] = pluginId
            attributes["Plugin-Version"] = version
            attributes["Plugin-Provider"] = pluginProvider
            attributes["Plugin-Dependencies"] = pluginDependencies
        }

        archiveFileName.set("$pluginId-$version.jar")

        dependencies {
            exclude(dependency("io.vertx:vertx-core"))
            exclude {
                it.moduleGroup == "io.netty" || it.moduleGroup == "org.slf4j"
            }
        }
    }

    register("copyJar") {
        pluginsDir?.let {
            doLast {
                copy {
                    from(shadowJar.get().archiveFile.get().asFile.absolutePath)
                    into(it)
                }
            }
        }

        outputs.upToDateWhen { false }
        mustRunAfter(shadowJar)
    }

    jar {
        enabled = false
        dependsOn(shadowJar)
        dependsOn("copyJar")
    }
}

publishing {
    repositories {
        maven {
            name = "parsek-plugin-auth-register-notify-webhook"
            url = uri("https://maven.pkg.github.com/StatuParsek/parsek-plugin-auth-register-notify-webhook")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME_GITHUB")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN_GITHUB")
            }
        }
    }

    publications {
        create<MavenPublication>("shadow") {
            project.extensions.configure<com.github.jengelman.gradle.plugins.shadow.ShadowExtension> {
                artifactId = "parsek-plugin-auth-register-notify-webhook"
                component(this@create)
            }
        }
    }
}