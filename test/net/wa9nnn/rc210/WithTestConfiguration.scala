/*
 * Copyright (C) 2023  Dick Lieber, WA9NNN                               
 *                                                                       
 * This program is free software: you can redistribute it and/or modify  
 * it under the terms of the GNU General Public License as published by  
 * the Free Software Foundation, either version 3 of the License, or     
 * (at your option) any later version.                                   
 *                                                                       
 * This program is distributed in the hope that it will be useful,       
 * but WITHOUT ANY WARRANTY; without even the implied warranty of        
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         
 * GNU General Public License for more details.                          
 *                                                                       
 * You should have received a copy of the GNU General Public License     
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package net.wa9nnn.rc210

import com.typesafe.config.{Config, ConfigFactory}
import net.wa9nnn.rc210.util.Configs
import org.apache.commons.io.FileUtils

import java.io.File
import java.nio.file.Files

class WithTestConfiguration extends RcSpec {

  implicit val config: Config = ConfigFactory.parseResources("test.conf")
    .withFallback(ConfigFactory.load())
    .resolve()
  try

    if (config.getString("configfile") != "test.conf")
      throw new IllegalStateException("Must be using test.conf")
  catch {
    case e: Exception =>
      e.printStackTrace()
  }

  def cleanDirectory(): Unit = {
    val datadir = Configs.path("vizRc210.dataDir")
    if (Files.exists(datadir))
      FileUtils.cleanDirectory(datadir.toFile)
  }
}
