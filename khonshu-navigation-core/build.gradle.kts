@file:Suppress("ClassName")

import java.net.URL
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
  alias(libs.plugins.kotlin.multiplatform)

  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.parcelize)

  alias(libs.plugins.jetbrains.compose)
  alias(libs.plugins.poko)

  alias(libs.plugins.vanniktech.maven.publish)
  alias(libs.plugins.dokka)
  alias(libs.plugins.kotlinx.binary.compatibility.validator)
  alias(libs.plugins.kotlinx.kover)
}

compose {
  kotlinCompilerPlugin.set(libs.versions.jetbrains.compose.compiler)
}

kotlin {
  explicitApi()

  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(libs.versions.java.toolchain.get()))
    vendor.set(JvmVendorSpec.AZUL)
  }

  androidTarget {
    publishAllLibraryVariants()

    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.target.get()))
      }
    }
  }

  jvm {
    compilations.configureEach {
      compilerOptions.configure {
        jvmTarget.set(JvmTarget.fromTarget(libs.versions.java.target.get()))
      }
    }
  }

  js(IR) {
    compilations.all {
      kotlinOptions {
        sourceMap = true
        moduleKind = "commonjs"
      }
    }
    browser()
    nodejs()
  }

  iosArm64()
  iosX64()
  iosSimulatorArm64()

  macosX64()
  macosArm64()

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain {
      dependencies {
        api(compose.runtime)
        api(compose.ui)

        api(projects.lifecycle)
        api(libs.kmp.viewmodel.core)
        api(libs.kmp.viewmodel.savedstate)
        api(libs.kmp.viewmodel.compose)
      }
    }
    commonTest {
      dependencies {
        implementation(kotlin("test-common"))
        implementation(kotlin("test-annotations-common"))
      }
    }

    val commonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    val commonJvmTest by creating {
      dependsOn(commonTest.get())
    }

    val nonJvmMain by creating {
      dependsOn(commonMain.get())
    }
    val nonJvmTest by creating {
      dependsOn(commonTest.get())
    }

    androidMain {
      dependsOn(commonJvmMain)

      dependencies {
        implementation(libs.androidx.activity.compose)
      }
    }
    val androidUnitTest by getting {
      dependsOn(commonJvmTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }

    val nonAndroidMain by creating {
      dependsOn(commonMain.get())
    }
    val nonAndroidTest by creating {
      dependsOn(commonTest.get())
    }

    jvmMain {
      dependsOn(nonAndroidMain)
      dependsOn(commonJvmMain)
    }
    jvmTest {
      dependsOn(nonAndroidTest)
      dependsOn(commonJvmTest)

      dependencies {
        implementation(kotlin("test-junit"))
      }
    }

    jsMain {
      dependsOn(nonAndroidMain)
      dependsOn(nonJvmMain)
    }
    jsTest {
      dependsOn(nonAndroidTest)
      dependsOn(nonJvmTest)

      dependencies {
        implementation(kotlin("test-js"))
      }
    }

    nativeMain {
      dependsOn(nonAndroidMain)
      dependsOn(nonJvmMain)
    }
    nativeTest {
      dependsOn(nonAndroidTest)
      dependsOn(nonJvmTest)
    }
  }

  sourceSets.matching { it.name.contains("Test") }.all {
    languageSettings {
      optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
    }
  }

  sourceSets.configureEach {
    languageSettings {
      optIn("com.hoc081098.solivagant.navigation.internal.InternalNavigationApi")
    }
  }
}

tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<*>>().configureEach {
  kotlinOptions {
    // 'expect'/'actual' classes (including interfaces, objects, annotations, enums,
    // and 'actual' typealiases) are in Beta.
    // You can use -Xexpect-actual-classes flag to suppress this warning.
    // Also see: https://youtrack.jetbrains.com/issue/KT-61573
    freeCompilerArgs +=
      listOf(
        "-Xexpect-actual-classes",
      )
  }
}

android {
  compileSdk = libs.versions.android.compile.get().toInt()
  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  namespace = "com.hoc081098.solivagant.khonshu.navigation.core"

  defaultConfig {
    minSdk = libs.versions.android.min.get().toInt()
  }

  // still needed for Android projects despite toolchain
  compileOptions {
    sourceCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
    targetCompatibility = JavaVersion.toVersion(libs.versions.java.target.get())
  }
}

mavenPublishing {
  publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01, automaticRelease = true)
  signAllPublications()
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask>().configureEach {
  dokkaSourceSets {
    configureEach {
      externalDocumentationLink("https://kotlinlang.org/api/kotlinx.coroutines/")

      sourceLink {
        localDirectory.set(projectDir.resolve("src"))
        remoteUrl.set(URL("https://github.com/hoc081098/solivagant/tree/master/viewmodel-compose/src"))
        remoteLineSuffix.set("#L")
      }
    }
  }
}
