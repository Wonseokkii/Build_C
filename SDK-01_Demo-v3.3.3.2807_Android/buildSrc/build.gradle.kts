
plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.6.21"
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    api (gradleApi())
    api ("org.ow2.asm:asm:9.3")
    api ("org.ow2.asm:asm-commons:9.3")
    api ("commons-io:commons-io:2.7")
    api ("commons-codec:commons-codec:1.15")
    api ("com.android.tools.build:gradle:8.7.3")
    api ("com.android.tools:sdk-common:31.1.1")
}