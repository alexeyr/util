import sbt._
import com.twitter.sbt._
import Configurations._

class Project(info: ProjectInfo)
  extends StandardParentProject(info)
  with SubversionPublisher
  with ParentProjectDependencies
  with DefaultRepos
{
  val localMaven = Resolver.file("Local Maven repository", new java.io.File(Path.userHome+"/.m2/repository"))
  val publishTo = localMaven 
 
  override def usesMavenStyleBasePatternInPublishLocalConfiguration = true
  
  override def subversionRepository = Some("https://svn.twitter.biz/maven-public")
  val twitterRepo = "twitter.com" at "http://maven.twttr.com"

  val specsVersion = "1.6.9" 
  val versionSuffix = "_" + buildScalaVersion
  override def repositories = super.repositories - ("atlassian" at "https://m2proxy.atlassian.com/repository/public/")

  // Projects

  // util-core: extensions with no external dependency requirements
  val coreProject = project(
    "util-core", "util-core" + versionSuffix,
    new CoreProject(_))

  val evalProject = project(
    "util-eval", "util-eval" + versionSuffix,
    new EvalProject(_), coreProject)

  val codecProject = project(
    "util-codec", "util-codec" + versionSuffix,
    new CodecProject(_), coreProject)

  val collectionProject = project(
    "util-collection", "util-collection" + versionSuffix,
    new CollectionProject(_), coreProject)

  // util-reflect: runtime reflection and dynamic helpers
  val reflectProject = project(
    "util-reflect", "util-reflect" + versionSuffix,
    new ReflectProject(_), coreProject)

  // util-logging: logging wrappers and configuration
  val loggingProject = project(
    "util-logging", "util-logging" + versionSuffix,
    new LoggingProject(_), coreProject)

  // util-thrift: thrift (serialization) utilities
  val thriftProject = project(
    "util-thrift", "util-thrift" + versionSuffix,
    new ThriftProject(_), coreProject, codecProject)

  // util-hashing: hashing and distribution utilities
  val hashingProject = project(
    "util-hashing", "util-hashing" + versionSuffix,
    new HashingProject(_), coreProject)

  class CoreProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    override def compileOrder = CompileOrder.Mixed
  }

  class EvalProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    val scalaTools = "org.scala-lang" % "scala-compiler" % buildScalaVersion % "compile"
    override def filterScalaJars = false
  }

  class CodecProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    val commonsCodec = "commons-codec" % "commons-codec" % "1.5"
  }

  class CollectionProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    val guava              = "com.google.guava"    % "guava"               % "r09"
    val commonsCollections = "commons-collections" % "commons-collections" % "3.2.1"
  }

  class ReflectProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    val asm = "asm" % "asm" % "3.3.1"
    val asmUtil = "asm" % "asm-util" % "3.3.1"
    val asmCommons = "asm" % "asm-commons" % "3.3.1"
    val cglib = "cglib" % "cglib" % "2.2"
  }

  class LoggingProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    val compileWithSpecs = "org.scala-tools.testing" %% "specs" % specsVersion % "provided"
  }

  class ThriftProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    override def compileOrder = CompileOrder.JavaThenScala
    val thrift = "thrift"        % "libthrift"     % "0.5.0"
    val slf4j  = "org.slf4j"     % "slf4j-nop"     % "1.5.8" % "provided"
    val jacksonCore     = "org.codehaus.jackson" % "jackson-core-asl"   % "1.8.1"
    val jacksonMapper   = "org.codehaus.jackson" % "jackson-mapper-asl" % "1.8.1"
  }

  class HashingProject(info: ProjectInfo)
    extends StandardProject(info)
    with ProjectDefaults
  {
    val commonsCodec = "commons-codec" % "commons-codec" % "1.5" % "test"
  }

  trait ProjectDefaults
    extends StandardProject
    with SubversionPublisher
    with PublishSite
    with ProjectDependencies
    with DefaultRepos
  {
    val specs   = "org.scala-tools.testing" %% "specs" % specsVersion % "test" withSources()
    val mockito = "org.mockito"             % "mockito-all" % "1.8.5" % "test" withSources()
    val junit   = "junit"                   %       "junit" % "3.8.2" % "test"

    override def compileOptions = super.compileOptions ++ Seq(Unchecked) ++
      compileOptions("-encoding", "utf8") ++
      compileOptions("-deprecation")
    override def repositories = super.repositories - ("atlassian" at "https://m2proxy.atlassian.com/repository/public/")
  }
}
