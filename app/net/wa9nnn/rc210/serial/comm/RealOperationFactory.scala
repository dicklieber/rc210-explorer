package net.wa9nnn.rc210.serial.comm
import com.fazecast.jSerialComm.SerialPort

/**
 * Uses the acual (real) serial port.
 */
class RealOperationFactory extends OperationFactory {

  def openStreamBased(serialPort: SerialPort): StreamBased =
    new RealStreamBased(serialPort)

  def openEventBased(serialPort: SerialPort): EventBased =
    new RealEventBased(serialPort)
}
