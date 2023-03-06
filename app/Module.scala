
import net.codingwell.scalaguice.{ScalaModule, ScalaMultibinder}
import net.wa9nnn.rc210.MemoryExtractor
import net.wa9nnn.rc210.data.macros.MacroExtractor
import net.wa9nnn.rc210.data.schedules.ScheduleExtractor
import net.wa9nnn.rc210.data.vocabulary.MessageMacroExtractor
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
class Module(environment: Environment, configuration: Configuration) extends ScalaModule {

  override def configure(): Unit = {

    install(new ConfigModule(configuration))
    // Got to be a way to this this automatically without having to specify each implementation.
/*
    val publishers = ScalaMultibinder.newSetBinder[MemoryExtractor](binder)
    publishers.addBinding.to[MacroExtractor]
    publishers.addBinding.to[ScheduleExtractor]
    publishers.addBinding.to[MessageMacroExtractor]
*/


  }
}

