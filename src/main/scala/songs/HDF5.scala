package songs

import java.io.File
import ch.systemsx.cisd.hdf5._
import scala.util._

object HDF5 {

  case class H5CDS(ds: HDF5CompoundDataMap){
    def vector[T](colName: String): Vector[T] = ds.get(colName).asInstanceOf[Array[T]].toVector
    def get[T](colName: String): T = ds.get(colName).asInstanceOf[T]
  }

  def getCompoundDS(h5: IHDF5Reader, path: String): H5CDS = {
    val data = h5.compound().read(path, classOf[HDF5CompoundDataMap])
    H5CDS(data)
  }

  def open(filename: String): Try[IHDF5Reader] = Try{HDF5FactoryProvider.get().openForReading(new File(filename))}

  def close(h5: IHDF5Reader): Try[Unit] = Try{h5.close()}

  // read a vector from array of vectors
  def readArray[T](data: Vector[T], offset: Vector[Int], i: Int): Vector[T] = {
    val j0: Int = offset(i)
    val j1: Int = offset.lift(i+1).getOrElse(data.length)
    data.slice(j0,j1)
  }

}
