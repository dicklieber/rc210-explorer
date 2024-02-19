package net.wa9nnn.rc210

import os.*

/**
 * Create a temp directory and fill with files from test/resources/dataDir.
 *
 * @return path to the directory.
 */
def createTempDataDirectory(): Path =
  val source: Path = Path(getClass.getResource("/dataDir").getFile)

//  val source: ResourcePath = os.resource / "dataDir"
  val dataDirectory: Path = os.temp.dir(prefix = "testdataDir")
  os.copy(source, dataDirectory, mergeFolders = true, copyAttributes = true)
  dataDirectory
