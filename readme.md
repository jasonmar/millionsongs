## Description

This project provides an example of loading a dataset from HDF5 and converting to a case class and then to a Spark DataFrame for use with Spark MLLib.
A sample pipeline for Linear Regression is provided.

## Motivation

The goal of creating this sample application was to evaluate the hdf5 library and use of Spark's MLLib Pipeline.

## Features

  * Reads million songs dataset from HDF5
  * Provides case classes for million songs dataset
  * Uses pipeline using OneHotEncoder and VectorAssembler
  * Trains MLLib LinearRegressionModel and calculates mean squared error

## License

This project uses the Apache 2.0 license. Read LICENSE file.

## Authors and Copyright

Copyright (C) 2016 Jason Mar