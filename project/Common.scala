package akka.grpc

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import akka.grpc.Dependencies.Versions.{ scala212, scala213 }
import com.lightbend.paradox.projectinfo.ParadoxProjectInfoPluginKeys.projectInfoVersion
import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile
import com.typesafe.tools.mima.plugin.MimaKeys._
import sbtprotoc.ProtocPlugin.autoImport.PB
import xerial.sbt.Sonatype
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

object Common extends AutoPlugin {
  override def trigger = allRequirements

  override def requires = JvmPlugin && Sonatype

  private val consoleDisabledOptions = Seq("-Xfatal-warnings", "-Ywarn-unused", "-Ywarn-unused-import")

  override def globalSettings =
    Seq(
      organization := "com.lightbend.akka.grpc",
      organizationName := "Lightbend Inc.",
      organizationHomepage := Some(url("https://www.lightbend.com/")),
      resolvers += Resolver.sonatypeRepo("staging"),
      homepage := Some(url("https://akka.io/")),
      scmInfo := Some(ScmInfo(url("https://github.com/akka/akka-grpc"), "git@github.com:akka/akka-grpc")),
      developers += Developer(
        "contributors",
        "Contributors",
        "https://gitter.im/akka/dev",
        url("https://github.com/akka/akka-grpc/graphs/contributors")),
      licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
      description := "Akka gRPC - Support for building streaming gRPC servers and clients on top of Akka Streams.")

  override lazy val projectSettings = Seq(
    projectInfoVersion := (if (isSnapshot.value) "snapshot" else version.value),
    sonatypeProfileName := "com.lightbend",
    scalacOptions ++= List(
      "-unchecked",
      "-deprecation",
      "-language:_",
      "-Xfatal-warnings",
      "-Ywarn-unused",
      "-encoding",
      "UTF-8"),
    Compile / scalacOptions ++= Seq(
      // generated code for methods/fields marked 'deprecated'
      "-P:silencer:globalFilters=Marked as deprecated in proto file",
      // generated scaladoc sometimes has this problem
      "-P:silencer:globalFilters=unbalanced or unclosed heading",
      // deprecated in 2.13, but used as long as we support 2.12
      "-P:silencer:globalFilters=Use `scala.jdk.CollectionConverters` instead",
      "-P:silencer:globalFilters=Use LazyList instead of Stream",
      // ignore imports in templates
      "-P:silencer:pathFilters=.*.txt"),
    Compile / console / scalacOptions ~= (_.filterNot(consoleDisabledOptions.contains)),
    javacOptions ++= List("-Xlint:unchecked", "-Xlint:deprecation"),
    Compile / doc / scalacOptions := scalacOptions.value ++ Seq(
      "-doc-title",
      "Akka gRPC",
      "-doc-version",
      version.value,
      "-sourcepath",
      (ThisBuild / baseDirectory).value.toString,
      "-skip-packages",
      "akka.pattern:" + // for some reason Scaladoc creates this
      "templates",
      "-doc-source-url", {
        val branch = if (isSnapshot.value) "main" else s"v${version.value}"
        s"https://github.com/akka/akka-grpc/tree/${branch}€{FILE_PATH_EXT}#L€{FILE_LINE}"
      },
      "-doc-canonical-base-url",
      "https://doc.akka.io/api/akka-grpc/current/"),
    Compile / doc / scalacOptions -= "-Xfatal-warnings",
    apiURL := Some(url(s"https://doc.akka.io/api/akka-grpc/${projectInfoVersion.value}/akka/grpc/index.html")),
    libraryDependencies ++= Seq(
      compilerPlugin(("com.github.ghik" % "silencer-plugin" % Dependencies.Versions.silencer).cross(CrossVersion.full)),
      ("com.github.ghik" % "silencer-lib" % Dependencies.Versions.silencer % Provided).cross(CrossVersion.full)),
    (Test / testOptions) += Tests.Argument(TestFrameworks.ScalaTest, "-oDF"),
    crossScalaVersions := Seq(scala212, scala213),
    mimaReportSignatureProblems := true,
    scalafmtOnCompile := true)
}
