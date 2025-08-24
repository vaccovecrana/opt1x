plugins {
  id("io.vacco.ronove") version "1.5.0"
  application
}

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  sharedLibrary(false, false)
  addJ8Spec()
}

configure<io.vacco.ronove.plugin.RvPluginExtension> {
  optionalFields = true
  controllerClasses = arrayOf("io.vacco.opt1x.web.OtApiHdl")
  outFile.set(file("../ot-ui/@ui/rpc.ts"))
}

val api by configurations
val mtVer = "3.7.1"

dependencies {
  api("com.google.code.gson:gson:2.11.0")
  api("am.ik.yavi:yavi:0.14.1")
  api("io.vacco.shax:shax:2.0.16.0.4.3")
  api("io.vacco.ronove:rv-kit-murmux:1.5.0")
  api("org.codejargon.feather:feather:1.0")
  api("io.vacco.metolithe:mt-core:${mtVer}")
  api("io.vacco.jwt:jwt:0.8.0")
  api("org.hsqldb:hsqldb:2.7.4")
  api("io.rqlite:rqlite-jdbc:8.42.0.1")
  api("com.zaxxer:HikariCP:5.0.1")

  testImplementation("io.vacco.metolithe:mt-codegen:${mtVer}")
}
