package songs

import ncsa.hdf.`object`.FileFormat
import ncsa.hdf.`object`.h5._
import ncsa.hdf.`object`.Datatype
import scala.util._

object HDF5 {

  val DATATYPES = Map[Int,String](
    -1 -> "CLASS_NO_CLASS"
    ,0 -> "CLASS_INTEGER"
    ,1 -> "CLASS_FLOAT"
    ,2 -> "CLASS_CHAR"
    ,3 -> "CLASS_STRING"
    ,4 -> "CLASS_BITFIELD"
    ,5 -> "CLASS_OPAQUE"
    ,6 -> "CLASS_COMPOUND"
    ,7 -> "CLASS_REFERENCE"
    ,8 -> "CLASS_ENUM"
    ,9 -> "CLASS_VLEN"
    ,10 -> "CLASS_ARRAY"
    ,11 -> "CLASS_TIME"
  )

  case class H5CDS(ds: H5CompoundDS){
    val length = ds.getHeight
    def indices = 0 until length
    val memberIds: Map[String,Int] = ds.getMemberNames.zipWithIndex.toMap
    val columns: Vector[Object] = ds.getData.asInstanceOf[java.util.List[Object]].toArray.toVector
    val members: Map[String,Object] = memberIds.map{t => (t._1,columns(t._2))}
    lazy val types: Vector[Datatype] = ds.getMemberTypes.toVector

    def toVector[T](colName: String): Vector[T] = members(colName).asInstanceOf[Array[T]].toVector
  }

  case class H5SDS(ds: H5ScalarDS){
    def toVector[T]: Vector[T] = ds.getData.asInstanceOf[Array[T]].toVector
  }

  def getCompoundDS(h5: H5File, path: String): H5CDS = {
    val ds = h5.get(path).asInstanceOf[H5CompoundDS]
    ds.init()
    H5CDS(ds)
  }

  def getScalarDS(h5: H5File, path: String): H5SDS = {
    val ds = h5.get(path).asInstanceOf[H5ScalarDS]
    ds.init()
    H5SDS(ds)
  }

  def open(filename: String): Try[H5File] = Try{new H5File(filename, FileFormat.READ)}

  def close(h5: H5File): Try[Unit] = Try{h5.close()}

  // read a vector from array of vectors
  def readArray[T](data: Vector[T], offset: Vector[Int], i: Int): Vector[T] = {
    val j0: Int = offset(i)
    val j1: Int = offset.lift(i+1).getOrElse(data.length)
    data.slice(j0,j1)
  }

}
