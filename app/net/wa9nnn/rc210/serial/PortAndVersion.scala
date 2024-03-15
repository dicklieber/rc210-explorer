package net.wa9nnn.rc210.serial

import com.fazecast.jSerialComm.SerialPort
import com.wa9nnn.wa9nnnutil.tableui.{Cell, Header, Table}
import play.api.routing.Router.empty.routes

case class PortAndVersion(
                           selectedSerialPort: Option[SerialPort] = None,
                           version: Option[String] = None):
  def selectedInfo: String =
    selectedSerialPort.map { serialPort =>
      s"${serialPort.getSystemPortName} (${serialPort.getDescriptivePortName}) v:${version.getOrElse("?")}"
    }.getOrElse("No port selected!")

  def table: Table =
    Table(Header.none,
      Seq(
        "Port:" -> Cell(selectedSerialPort.map(_.getSystemPortName).getOrElse("Not selected!"))
          .withUrl(controllers.routes.IOController.listSerialPorts.url),
        "Version:" -> version.getOrElse("?")
      )
    )

object PortAndVersion:
  def apply(serialPort: SerialPort): PortAndVersion =
    new PortAndVersion(Option(serialPort))

