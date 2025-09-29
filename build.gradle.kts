plugins { id("io.vacco.oss.gitflow") version "1.8.3" apply(false) }

subprojects {
  apply(plugin = "io.vacco.oss.gitflow")
  group = "io.vacco.opt1x"
  version = "0.6.6"

  configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
    addClasspathHell()
  }

  configure<io.vacco.cphell.ChPluginExtension> {
    resourceExclusions.add("module-info.class")
    resourceExclusions.add("LICENSE")
  }
}
