package net.wa9nnn.rc210.data.macros

import com.typesafe.scalalogging.LazyLogging
import net.wa9nnn.rc210.{DatSection, DataItem}

import scala.util.Try

class MacroTree(datSection: DatSection) {
  assert(datSection.sectionName == "Macros")
}

