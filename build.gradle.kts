import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
	java
	application
	kotlin("jvm") version "2.0.0"
}

application {
	mainClass.set("com.sprend.himeji.Main")
}

repositories {
	mavenCentral()
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

kotlin {
	jvmToolchain(17)
}

tasks.withType<JavaCompile> {
	options.release.set(17)
	options.encoding = "UTF-8"
}

tasks.withType<KotlinJvmCompile>().configureEach {
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
	}
}

tasks.test {
	useJUnitPlatform()
}

dependencies {
	implementation(kotlin("stdlib"))
	implementation("org.apache.poi:poi-ooxml:5.4.0")
	implementation("joda-time:joda-time:2.14.0")

	testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
