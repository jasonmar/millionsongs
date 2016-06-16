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


# Amazon Linux AMI setup instructions

## Login to the instance
```
ssh -i ~/.ssh/SSHKey.pem ec2-user@aws-spot-instance
```

## Install packages
```
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
wget https://archive.cloudera.com/cdh5/one-click-install/redhat/7/x86_64/cloudera-cdh-5-0.x86_64.rpm
sudo yum --nogpgcheck localinstall cloudera-cdh-5-0.x86_64.rpm
sudo yum update
sudo yum install -y java-1.8.0-openjdk java-1.8.0-openjdk-devel.x86_64 git ant apache-ivy sbt spark-core spark-master spark-worker
sudo yum remove java-1.7.0-openjdk
```

## Stop Spark cluster
```
sudo service spark-worker stop
sudo service spark-master stop
```

## Download JHDF5 library
```
wget -O sis-jhdf5-14.12.6-r36356.zip https://wiki-bsse.ethz.ch/download/attachments/26609237/sis-jhdf5-14.12.6-r36356.zip?version=1&modificationDate=1462044819824&api=v2
unzip sis-jhdf5-14.12.6-r36356.zip
```

## Publish JHDF5 library to local ivy repository so that we can use sbt to build
```
mkdir -p ~/lib/core
cd ~/lib
echo '<?xml version="1.0" encoding="utf-8"?>
<project name="localrepository" default="publish" xmlns:ivy="antlib:org.apache.ivy.ant">
        <target name="publish" description="Publish this build into repository">
        <ivy:resolve/>
        <ivy:publish pubrevision="1.0.0" status="release" resolver="local" overwrite="true" >
                <artifacts pattern="[artifact].[ext]"/>
        </ivy:publish>
        </target>
</project>' | tee -a build.xml

cd ~/lib/core
ln -s ../build.xml build.xml
cp ~/sis-jhdf5/lib/sis-jhdf5-core.jar .
echo '<?xml version="1.0" encoding="utf-8"?>
<ivy-module version="1.0">
        <info organisation="ch.ethz" module="sis-jhdf5-core" revision="1.0.0" />
        <publications>
                <artifact name="sis-jhdf5-core" type="jar" ext="jar" />
        </publications>
        <dependencies>
                <dependency org="commons-lang" name="commons-lang" rev="2.6" />
                <dependency org="commons-io" name="commons-io" rev="2.4" />
        </dependencies>
</ivy-module>' | tee -a ivy.xml
ant

mkdir -p ~/lib/base
cd ~/lib/base
ln -s ../build.xml build.xml
cp ~/sis-jhdf5/lib/sis-base.jar .

echo '<?xml version="1.0" encoding="utf-8"?>
<ivy-module version="1.0">
        <info organisation="ch.ethz" module="sis-base" revision="1.0.0" />
        <publications>
                <artifact name="sis-base" type="jar" ext="jar" />
        </publications>
        <dependencies>
                <dependency org="commons-lang" name="commons-lang" rev="2.6" />
                <dependency org="commons-io" name="commons-io" rev="2.4" />
        </dependencies>
</ivy-module>' | tee -a ivy.xml
ant
```

## Make native libraries available to Spark
```
echo 'export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/home/ec2-user/sis-jhdf5/lib/native/jhdf5/amd64-Linux:/home/ec2-user/nativelib/lib"' | sudo tee -a /etc/spark/conf.dist/spark-env.sh
```

## Fetch dataset
```
wget http://static.echonest.com/millionsongsubset_full.tar.gz
tar xfzv millionsongsubset_full.tar.gz
```

## Build the jar
```
git clone https://github.com/jasonmar/millionsongs
cd millionsongs
sbt assembly
```

## Test the application in the REPL
```
env JAVA_OPTS="-Xms6G -Xmx6G -XX:+UseConcMarkSweepGC -Djava.library.path=/home/ec2-user/sis-jhdf5/lib/native/jhdf5/amd64-Linux" sbt console
```

```
import songs._
val inputDir="/home/ec2-user/MillionSongSubset/data"
val files: Vector[String] = Files.getPaths(inputDir)
val f = HDF5.open(files.head)
f.toOption.map(ReadSong.readSongs).flatMap(_.toOption).map(SongML.extractFeatures)
```


## Start the Spark cluster
```
sudo service spark-master start
```

## Add hostname to hosts file to allow name resolution
```
ip=$(/sbin/ifconfig | grep -A 1 'eth0' | grep inet | awk {'print $2'} | awk -F: {'print $2'})
hostname=$(hostname)
echo "$ip $hostname" | sudo tee -a /etc/hosts
```


## Submit application to run on cluster
```
spark-submit --class "songs.Main" --master local[6] /home/ec2-user/millionsongs/target/scala-2.10/songs.jar
spark-submit --class "songs.TrainModel" --master local[6] /home/ec2-user/millionsongs/target/scala-2.10/songs.jar
spark-submit --class "songs.EvaluateModel" --master local[6] /home/ec2-user/millionsongs/target/scala-2.10/songs.jar
```


## Example Training output
```
Model coefficients:
duration:       0.00129
loudness:       0.01917
end_of_fade_in: -0.01008
start_of_fade_out:      0.00468
tempo:  0.00195
key:    0.00006
mode:   -0.00507
pitchRange:     0.00288
timbreRange:    -0.00560
year:   0.04625



Coefficient t-values:
duration:       0.13668
loudness:       5.33766
end_of_fade_in: -2.91448
start_of_fade_out:      0.49697
tempo:  0.56150
key:    0.01631
mode:   -1.46666
pitchRange:     15.53672
timbreRange:    -1.61230
year:   13.53197



Training Metrics
Explained Variance:     0.002851644924233068
R^2:                    0.16146
MSE:                    0.01682
RMSE:                   0.12968
```




## Example Testing output
```
Model coefficients:
duration:       0.00129
loudness:       0.01917
end_of_fade_in: -0.01008
start_of_fade_out:      0.00468
tempo:  0.00195
key:    0.00006
mode:   -0.00507
pitchRange:     0.00288
timbreRange:    -0.00560
year:   0.04625



Testing Metrics
Explained Variance:     0.0028209709941955418
R^2:                    0.17676
MSE:                    0.01503
RMSE:                   0.12260
```


# Conclusion
Larger relative absolute t-statistics may indicate that pitch range, year, loudness, and fade-in time are the most likely to impact the outcome variable.

Taking this information into account, artists should release tracks which are have a wide range of pitch, are new, loud, and have a shorter fade-in time.
A higher tempo and longer duration also seem to be helpful.


# Notes
Reduced features were given to the Parameter Grid used by the CrossValidator class, but this did not improve the model performance.

Interestingly, the model performs better on the test dataset than the training dataset with respect to R-squared.
