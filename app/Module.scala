
import com.google.inject.{AbstractModule, Binder}
import play.api.{Configuration, Environment}
import com.typesafe.scalalogging.LazyLogging

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
class Module(environment: Environment, configuration: Configuration) extends AbstractModule with LazyLogging{
  override def configure(): Unit = {
    install(RcActorsModule)
  }
}


