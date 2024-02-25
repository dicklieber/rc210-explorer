package net.wa9nnn.rc210.ui.nav

import enumeratum.{EnumEntry, PlayEnum}
import net.wa9nnn.rc210.ui.{Tab, Tabx}

sealed trait TabKind(val iconName: String, val toolTip: String) extends EnumEntry:
  val noTab: Tab =
    Tabx("none", "", "", this)

object TabKind extends PlayEnum[TabKind] {

  override val values: IndexedSeq[TabKind] = findValues

  case object Fields extends TabKind("bi-database", "Edit RC-210 fields.")

  case object Rc210Io extends TabKind("bi-arrow-down-up", "Input/Output with RC-210. e.g. Upload, Download, Clock etc.")
  case object Disk extends TabKind("bi-hdd", "Save, load files.")

  case object Settings extends TabKind("bi-gear-wide-connected", "Users")

  case object Debug extends TabKind("bi-question-diamond", "Debug Tools")

}