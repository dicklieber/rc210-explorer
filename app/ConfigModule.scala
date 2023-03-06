
import com.typesafe.config.ConfigValueType._
import com.typesafe.config.{Config, ConfigValue}
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.BindingExtensions._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration

import java.util.Map.Entry
import scala.jdk.CollectionConverters._


/**
 *  With thanks to https://github.com/allenai/common/blob/main/guice/src/main/scala/org/allenai/common/guice/ConfigModule.scala
 * This module binds each property defined in the active typesafe config to
 * a ''@Named("<key>"'' annotation.  This makes any config property value
 * accessible via injection by annotating a constructor parameter with the desired
 *
 * For example
 * {{{
 *                                                                class ExampleService @Inject()(@Named("example.timeout") timeout: Int) {
 *                                                                  ...
 *                                                                }
 * }}}
 * can be declared to inject timeout with the value obtained by calling
 * Config.getInt("example.timeout").
 *
 * Also binds the active configuration to the Config type so that injectable classes
 * can obtain the config object directly.
 *
 * For example
 * {{{
 *                                                                class ExampleService @Inject()(config: Config)) {
 *                                                                  timeout = config.get("example.timeout")
 *                                                                  ...
 *                                                                }
 * }}}
 * can be declared to inject config with the active Config object.
 *
 */
class ConfigModule(configuration: Configuration) extends ScalaModule with LazyLogging {
  override def configure(): Unit = {
    val config = configuration.underlying

    //    bind[Config].toInstance(config)
    bindConfig(config)
  }

  /**
   * Binds every entry in Config to a @Named annotation using the fully qualified
   * config key as the name.
   */
  private def bindConfig(config: Config): Unit = {
    var configSet: Set[String] = Set.empty

    def bindLevelOneConfig(entry: Entry[String, ConfigValue]): Unit = {

      val parts: Array[String] = entry.getKey.split("""\.""")
      if (parts.length > 1) {
        val topLevelName = parts.head
        if (!configSet.contains(topLevelName)) {
          configSet = configSet + topLevelName
          logger.trace(s"Need config: $topLevelName")
          var configSection: Config = null
          try {
            configSection = config.getConfig(topLevelName)
            bind[Config].annotatedWithName(topLevelName).toInstance(configSection)
            logger.debug(s"Bound configSection: $topLevelName")
          }
          catch {
            case e: Exception =>
              logger.error(s"Binding Config: $topLevelName", e)
          }
        }
      }
    }

    for (entry: Entry[String, ConfigValue] <- config.entrySet.asScala) {
      bindLevelOneConfig(entry)
      val cv = entry.getValue
      cv.valueType match {
        case STRING | NUMBER | BOOLEAN =>
          bindPrimitive(entry.getKey, entry.getValue)
        case LIST =>
          bindList(entry.getKey, entry.getValue)
        case NULL =>
          throw new AssertionError(
            s"Did not expect NULL entry in ConfigValue.entrySet: ${cv.origin}"
          )
        case OBJECT =>
          throw new AssertionError(
            s"Did not expect OBJECT entry in ConfigValue.entrySet: ${cv.origin}"
          )
      }
    }
  }

  /**
   * Bind a primitive value (Int, Double, Boolean, String) to a
   * '@Named("<key>") annotation.
   */
  private def bindPrimitive(key: String, value: ConfigValue): Unit = {
    val unwrapped = value.unwrapped.toString
    binderAccess.bindConstant.annotatedWithName(key).to(unwrapped)
  }

  /**
   * Bind a list value to '@Named("<key>") annotation.  This list contain any
   * primitive type.
   */
  private def bindList(key: String, value: ConfigValue): Unit = {
    val list = value.unwrapped.asInstanceOf[java.util.List[Any]].asScala
    if (list.isEmpty) {
      // Seq[Int|Double|Boolean] type params will only match a value bound as Seq[Any]
      bind[Seq[Any]].annotatedWithName(key).toInstance(Seq())
      // Seq[String] type params will only match a value bound as Seq[String]
      bind[Seq[String]].annotatedWithName(key).toInstance(Seq())
    }
    else {
      list.head match {
        case _: Integer =>
          val v = list.collect({ case x: java.lang.Integer => x.intValue })
          bind[Seq[Any]].annotatedWithName(key).toInstance(v.toList)
        case _: Double =>
          val v = list.collect({ case x: java.lang.Double => x.doubleValue })
          bind[Seq[Any]].annotatedWithName(key).toInstance(v.toList)
        case _: Boolean =>
          val v = list.collect({ case x: java.lang.Boolean => x.booleanValue })
          bind[Seq[Any]].annotatedWithName(key).toInstance(v.toList)
        case _: String =>
          val v = list.collect({ case x: String => x })
          bind[Seq[String]].annotatedWithName(key).toInstance(v.toList)
        case x =>
          logger.debug("Unsupported list type " + x.getClass)
      }
    }
  }
}

