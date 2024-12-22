/*
 * Copyright (c) 2015 HERE All rights reserved.
 */

import com.google.inject.AbstractModule
import com.typesafe.config.ConfigValueType.*
import com.typesafe.config.{Config, ConfigFactory, ConfigRenderOptions, ConfigValue}
import com.typesafe.scalalogging.LazyLogging
import net.codingwell.scalaguice.BindingExtensions.*
import net.codingwell.scalaguice.ScalaModule
import net.wa9nnn.rc210.ui.NamedKeyManager
import play.api.{Configuration, Environment}

import java.util
import java.util.Map.Entry
import scala.collection.mutable
import scala.jdk.CollectionConverters.*



class ConfigModule(env:Environment, configuration: Configuration) extends AbstractModule with ScalaModule with LazyLogging:
  println("ConfigModule")

  override def configure(): Unit =
    val config = configuration.underlying
//    bind[Config].toInstance(config)
    bindConfig(config)
    bind[NamedKeyManager].asEagerSingleton()

  /**
   * Binds every entry in Config to a @Named annotation using the fully qualified
   * config key as the name.
   */
  private def bindConfig(config: Config) =

    var configSet: Set[String] = Set.empty

    def bindLevelOneConfig(entry: Entry[String, ConfigValue]): Unit = {

      val parts: Array[String] = entry.getKey.split("""\.""")
      if (parts.length > 1)
      {
        val topLevelName = parts.head
        if (!configSet.contains(topLevelName))
        {
          configSet = configSet + topLevelName
          logger.trace(s"Need config: $topLevelName")
          var configSection: Config = null
          try

            configSection = config.getConfig(topLevelName)
            bind[Config].annotatedWithName(topLevelName).toInstance(configSection)
            logger.debug(s"Bound configSection: $topLevelName")
          catch
            case e: Exception =>
              logger.error(s"Binding Config: $topLevelName", e)
        }
      }
    }

    val rc210Config: Config = config.getConfig("vizRc210")
    val entries: mutable.Set[Entry[String, ConfigValue]] = rc210Config.entrySet().asScala

    for (entry: Entry[String, ConfigValue] <- entries)
    {
//      bindLevelOneConfig(entry)
      val configValue: ConfigValue = entry.getValue
      val valueType = configValue.valueType
      valueType match
      {
        case STRING | NUMBER | BOOLEAN =>
          bindPrimitive(entry.getKey, entry.getValue)
        case LIST =>
          bindList(entry.getKey, entry.getValue)
        case NULL =>
          throw new AssertionError(
            s"Did not expect NULL entry in ConfigValue.entrySet: ${configValue.origin}"
          )
        case OBJECT =>
          throw new AssertionError(
            s"Did not expect OBJECT entry in ConfigValue.entrySet: ${configValue.origin}"
          )
      }
    }

  /**
   * Bind a primitive value (Int, Double, Boolean, String) to a
   * '@Named("<key>") annotation.
   */
  private def bindPrimitive(key: String, value: ConfigValue) =
    val unwrapped = value.unwrapped.toString
    binderAccess.bindConstant.annotatedWithName(key).to(unwrapped)
    logger.trace(s"Bound primitive: $key = $unwrapped")

  /**
   * Bind a list value to '@Named("<key>") annotation.  This list contain any
   * primitive type.
   */
  private def bindList(key: String, value: ConfigValue) =
    val list: mutable.Seq[Any] = value.unwrapped.asInstanceOf[java.util.List[Any]].asScala
    if list.isEmpty then

      // Seq[Int|Double|Boolean] type params will only match a value bound as Seq[Any]
      bind[Seq[Any]].annotatedWithName(key).toInstance(Seq())
      // Seq[String] type params will only match a value bound as Seq[String]
      bind[Seq[String]].annotatedWithName(key).toInstance(Seq())

    else
      list.head match
      {
        case x: Integer =>
          val v = list.collect({ case x: java.lang.Integer => x.intValue }).toSeq
          bind[Seq[Any]].annotatedWithName(key).toInstance(v)
        case x: Double =>
          val v = list.collect({ case x: java.lang.Double => x.doubleValue }).toSeq
          bind[Seq[Any]].annotatedWithName(key).toInstance(v)
        case x: Boolean =>
          val v = list.collect({ case x: java.lang.Boolean => x.booleanValue }).toSeq
          bind[Seq[Any]].annotatedWithName(key).toInstance(v)
        case x: String =>
          val v = list.collect({ case x: String => x }).toSeq
          bind[Seq[String]].annotatedWithName(key).toInstance(v)
        case x =>
          logger.error("Unsupported list type " + x.getClass)
      }



