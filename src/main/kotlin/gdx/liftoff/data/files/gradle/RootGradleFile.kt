package gdx.liftoff.data.files.gradle

import gdx.liftoff.data.platforms.Android
import gdx.liftoff.data.platforms.TeaVM
import gdx.liftoff.data.project.Project

/**
 * Gradle file of the root project. Manages build script and global settings.
 */
class RootGradleFile(val project: Project) : GradleFile("") {
  val plugins = mutableSetOf<String>()
  private val buildRepositories = mutableSetOf<String>()

  init {
    buildRepositories.add("mavenCentral()")
    buildRepositories.add("maven { url 'https://s01.oss.sonatype.org' }")
    buildRepositories.add("mavenLocal()")
    buildRepositories.add("google()")
    buildRepositories.add("gradlePluginPortal()")
    buildRepositories.add("maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }")
    buildRepositories.add("maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }")
  }

  override fun getContent(): String = """buildscript {
  repositories {
${buildRepositories.joinToString(separator = "\n") { "    $it" }}
  }
  dependencies {
${joinDependencies(buildDependencies, type = "classpath", indent = "    ")}
  }
}

allprojects {
  apply plugin: 'eclipse'
  apply plugin: 'idea'
}

configure(subprojects${if (project.hasPlatform(Android.ID)) {
    " - project(':android')"
  } else {
    ""
  }}) {
${plugins.joinToString(separator = "\n") { "  apply plugin: '$it'" }}
  sourceCompatibility = ${project.advanced.javaVersion}
  compileJava {
    options.incremental = true
  }${if (project.hasPlatform(TeaVM.ID) && plugins.contains("kotlin")) {
    """
  compileKotlin {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
  }"""
  } else {
    ""
  }}${
  if (plugins.contains("kotlin")) {
    """

  kotlin {
    jvmToolchain(${project.advanced.javaVersion.removePrefix("1.")})
  }"""
  } else {
    ""
  }}
}

subprojects {
  version = '${project.advanced.version}'
  ext.appName = '${project.basic.name}'
  repositories {
    mavenCentral()
    maven { url 'https://s01.oss.sonatype.org' }
    // You may want to remove the following line if you have errors downloading dependencies.
    mavenLocal()
    maven { url 'https://oss.sonatype.org/content/repositories/snapshots/' }
    maven { url 'https://s01.oss.sonatype.org/content/repositories/snapshots/' }
    maven { url 'https://jitpack.io' }${
  if (project.hasPlatform(TeaVM.ID)) {
    "\n    maven { url 'https://teavm.org/maven/repository/' }"
  } else {
    ""
  }}
  }
}

eclipse.project.name = '${project.basic.name}' + '-parent'
"""
}
