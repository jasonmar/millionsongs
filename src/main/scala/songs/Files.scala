package songs

import java.io.File

object Files {

  def getPaths(path: String): Vector[String] = {
    val dir = new java.io.File(path)
    getFiles(dir).map(_.getAbsolutePath)
  }

  // Retrieve collection of all files within this directory
  def getFiles(dir: File): Vector[File] = {
    val dirs = collection.mutable.Stack[File]()
    val these = collection.mutable.ArrayBuffer[File]()
    dirs.push(dir)

    while (dirs.nonEmpty) {
      val dir = dirs.pop()
      val children = dir.listFiles
      val files = children.filterNot(_.isDirectory)
      val subDirectories = children.filter(_.isDirectory)
      these ++= files
      dirs.pushAll(subDirectories)
    }
    these.result().toVector
  }

}
