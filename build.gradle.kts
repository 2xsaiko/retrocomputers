plugins {
  kotlin("jvm")
}

base {
  archivesBaseName = "retrocomputers"
}

dependencies {
  compile(project(":hctm-base"))
}