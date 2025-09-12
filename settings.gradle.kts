pluginManagement {
  repositories 	{
    mavenCentral()
    gradlePluginPortal()
  }
}

include("ot-api", "ot-ui", "ot-app")

project(":ot-app").name = "opt1x-${System.getProperty("os.name").replace(" ", "").lowercase()}-${System.getProperty("os.arch")}"
