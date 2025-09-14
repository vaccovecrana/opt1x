plugins {
  application
  id("org.graalvm.buildtools.native") version "0.11.0"
}

dependencies {
  implementation(project(":ot-api"))
}

application { mainClass.set("io.vacco.opt1x.OtMain") }

graalvmNative {
  binaries {
    named("main") {
      configurationFileDirectories.from(file("src/main/resources"))
      buildArgs.addAll(
        "--enable-url-protocols=http,https",
        "-march=compatibility",
        "--no-fallback",
        "-H:+ReportExceptionStackTraces",
        "-R:MaxHeapSize=512m",
        "--initialize-at-build-time=java.xml,javax.xml.*"
      )
    }
  }
}

val copyLic = tasks.register<Copy>("copyLic") {
  from("../NOTICE", "../LICENSE")
  into("build/resources/main")
}

tasks.processResources {
  dependsOn(copyLic)
}
