plugins {
    java
}

group = "dev.dedsafio"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    // Paper 26.x: version dinamica de la ultima build de 26.3.
    // Para builds reproducibles, fija una concreta, p. ej. "26.3.build.237-stable"
    // (lista de builds: https://papermc.io/downloads/paper)
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(25)
}
