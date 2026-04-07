import org.gradle.internal.impldep.com.amazonaws.services.s3.transfer.Download
import java.util.regex.Pattern

tasks.create<Copy>("copyApp") {
  dependsOn(":Demo:assembleRelease")
  project.delete("${rootDir.absolutePath}/build/")
  from(rootDir.absolutePath + "/Demo/build/outputs/apk/release/") {
    exclude("*.json")
    destinationDir = File("${rootDir.absolutePath}/build/Release/demo_app/")
  }
}

tasks.create<Copy>("copyLibs") {
  dependsOn("copyApp")
  from(rootDir.absolutePath + "/base/libs") {
    destinationDir = File("${rootDir.absolutePath}/build/Release/libs")
  }
}

tasks.create<Copy>("copyDoc") {
  dependsOn("copyLibs")
  from("${rootDir.absolutePath}/libraries/sdk-core/doc/") {
    include("*.pdf")
    include("*.docx")
    include("Device User Manual/**")
  }
  into("${rootDir.absolutePath}/build/Release/doc")
}

tasks.create<Copy>("copyREADME") {
  dependsOn("copyDoc")
  var readmePath = "${rootDir.absolutePath}/libraries/sdk-core/doc/README.md"
  if (file(readmePath).exists()) {
    from(readmePath)
    into("${rootDir.absolutePath}/build/Release/")
  }
}

tasks.create<Copy>("copyScript") {
  dependsOn("copyREADME")
  from("${rootDir.absolutePath}/buildscript/") {
    include("dependencies.gradle")
  }
  into("${rootDir.absolutePath}/build/Release/demo_src/buildscript")
}

tasks.create<Copy>("copySource"){
  dependsOn("copyScript")
  from(rootDir.absolutePath) {
    exclude(".gradle")
    exclude("**/.gradle")
    exclude(".idea")
    exclude("**/.idea")
    exclude("*.properties")
    exclude("**/*.iml")
    exclude("*.gitignore")
    exclude("**/build")
    exclude("**/demo-engineer")
    exclude("**/libraries")
    exclude("**/local.properties")
    exclude("**/buildscript")
    exclude("release")
    exclude("vdireader")
  }

  eachFile {
    //相对于from root path
    if (this.path == "build.gradle") {
      filter { line: String ->
        if (line.contains("apply from: \"\$rootDir/buildscript/release.gradle.kts\"")) {
          ""
        } else {
          line
        }
      }
    }
  }


  filteringCharset = "UTF-8"

  into("${rootDir.absolutePath}/build/Release/demo_src")

}

tasks.register<Zip>("packageDistribution") {
  dependsOn("copySource")
  val target = getTargetPath()
  val src = getSrcPath()
  archiveFileName.set("$target.zip")
  destinationDirectory.set(file("${rootDir.absolutePath}/build/"))
  from(src)
}

fun getTargetPath(): String {
  var sdkVersion = findSDKVersion()
  return "${rootDir.absolutePath}/release/SDK-01_SDK_v${sdkVersion}_Android"
}

fun getSrcPath(): String {
  return "${rootDir.absolutePath}/build/Release"
}

fun findSDKVersion(): String {
  var version = ""
  val libsDirectory = File("${rootDir.absolutePath}/base/libs")
  libsDirectory.listFiles()?.forEach { file ->
    val p = Pattern.compile(".*(\\d+.\\d+.\\d).(\\d+.aar)$")
    val m = p.matcher(file.name)
    if (m.find()) {
      version = m.group(1).toString()
    }
  }
  return version
}
