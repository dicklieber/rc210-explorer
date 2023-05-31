
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 *
 * This uses scala-guice. https://github.com/codingwell/scala-guice for details.
 */
class Module(environment: Environment, configuration: Configuration) extends ScalaModule with AkkaGuiceSupport{
  override def configure(): Unit = {
    install(new ConfigModule(configuration))
    install(RcActorsModule)
  }
}

