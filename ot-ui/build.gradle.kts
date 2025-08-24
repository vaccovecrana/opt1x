import com.github.gradle.node.npm.task.NpmTask

plugins {
  id("io.vacco.oss.gitflow")
  id("com.github.node-gradle.node") version "7.0.1"
}

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  sharedLibrary(true, false)
}

val api by configurations

node {
  download.set(true)
  version.set("22.17.0")
}

val buildTaskUsingNpm = tasks.register<NpmTask>("buildNpm") {
  dependsOn(tasks.npmInstall)
  npmCommand.set(listOf("run", "build"))
  inputs.dir("./@ui")
  outputs.dir("./build/ui")
}

val copyJs = tasks.register<Copy>("copyJs") {
  dependsOn(buildTaskUsingNpm)
  from("./build/ui")
  from("./res/favicon.svg")
  from("./res/index.html")
  into("./build/resources/main/ui")
}

val copyTs = tasks.register<Copy>("copyTs") {
  dependsOn(buildTaskUsingNpm)
  from("./@ui")
  into("./build/resources/main/ui/@ui")
}

tasks.processResources {
  dependsOn(copyJs)
  dependsOn(copyTs)
}
