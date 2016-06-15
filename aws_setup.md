# Launch Spot Instance using Ubuntu AMI

Ubuntu 16.04 AMI's https://cloud-images.ubuntu.com/
us-west-2	xenial	16.04 LTS	amd64	instance-store	20160610	ami-e3708a83	aki-fc8f11cc
us-west-2	xenial	16.04 LTS	amd64	ebs-ssd	20160610	ami-516b9131	aki-fc8f11cc
us-west-2	xenial	16.04 LTS	amd64	hvm:instance-store	20160610	ami-e97d8789	hvm
us-west-2	xenial	16.04 LTS	amd64	hvm:ebs-ssd	20160610	ami-d06a90b0	hvm

For a spot instance launched on VPC, you must select c3 instance and an ebs-ssd AMI, otherwise the request will fail.


## Example spot request config.json
```
{
  "IamFleetRole": "arn:aws:iam::962968840636:role/aws-ec2-spot-fleet-role",
  "AllocationStrategy": "lowestPrice",
  "TargetCapacity": 1,
  "SpotPrice": "0.42",
  "ValidFrom": "2016-06-15T20:55:32Z",
  "ValidUntil": "2017-06-15T20:55:32Z",
  "TerminateInstancesWithExpiration": true,
  "LaunchSpecifications": [
    {
      "ImageId": "ami-d0f506b0",
      "InstanceType": "c3.xlarge",
      "KeyName": "20160608",
      "BlockDeviceMappings": [
        {
          "DeviceName": "/dev/xvda",
          "Ebs": {
            "DeleteOnTermination": true,
            "VolumeType": "gp2",
            "VolumeSize": 16,
            "SnapshotId": "snap-61034a9c"
          }
        }
      ],
      "NetworkInterfaces": [
        {
          "DeviceIndex": 0,
          "SubnetId": "subnet-8a1b89ee",
          "DeleteOnTermination": true,
          "AssociatePublicIpAddress": true,
          "Groups": [
            "sg-3775d951"
          ]
        }
      ]
    },
    {
      "ImageId": "ami-d0f506b0",
      "InstanceType": "c3.xlarge",
      "KeyName": "20160608",
      "BlockDeviceMappings": [
        {
          "DeviceName": "/dev/xvda",
          "Ebs": {
            "DeleteOnTermination": true,
            "VolumeType": "gp2",
            "VolumeSize": 16,
            "SnapshotId": "snap-61034a9c"
          }
        }
      ],
      "NetworkInterfaces": [
        {
          "DeviceIndex": 0,
          "SubnetId": "subnet-fa17a28c",
          "DeleteOnTermination": true,
          "AssociatePublicIpAddress": true,
          "Groups": [
            "sg-3775d951"
          ]
        }
      ]
    },
    {
      "ImageId": "ami-d0f506b0",
      "InstanceType": "c3.xlarge",
      "KeyName": "20160608",
      "BlockDeviceMappings": [
        {
          "DeviceName": "/dev/xvda",
          "Ebs": {
            "DeleteOnTermination": true,
            "VolumeType": "gp2",
            "VolumeSize": 16,
            "SnapshotId": "snap-61034a9c"
          }
        }
      ],
      "NetworkInterfaces": [
        {
          "DeviceIndex": 0,
          "SubnetId": "subnet-9c8079c4",
          "DeleteOnTermination": true,
          "AssociatePublicIpAddress": true,
          "Groups": [
            "sg-3775d951"
          ]
        }
      ]
    }
  ],
  "Type": "request"
}
```

# Amazon Linux

## Login to the instance
```
ssh -i ~/.ssh/SSHkey.pem ec2-user@aws-spot-instance
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

# Stop Spark cluster
```
sudo service spark-worker stop
sudo service spark-master stop
```



# Download JHDF5 library
```
wget -O sis-jhdf5-14.12.6-r36356.zip https://wiki-bsse.ethz.ch/download/attachments/26609237/sis-jhdf5-14.12.6-r36356.zip?version=1&modificationDate=1462044819824&api=v2
unzip sis-jhdf5-14.12.6-r36356.zip
```


# Publish JHDF5 library to local ivy repository
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


# Make native libraries available to Spark
```
echo 'export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:/home/ec2-user/sis-jhdf5/lib/native/jhdf5/amd64-Linux:/home/ec2-user/nativelib/lib"' | sudo tee -a /etc/spark/conf.dist/spark-env.sh
```



# Fetch dataset

```
wget http://static.echonest.com/millionsongsubset_full.tar.gz
tar xfzv millionsongsubset_full.tar.gz
mkdir -p MillionSongSubset/model
```



# Build the jar
```
git clone https://github.com/jasonmar/millionsongs
cd millionsongs
sbt assembly
```

# Test the application in the REPL
```
env JAVA_OPTS="-Xms6G -Xmx6G -XX:+UseConcMarkSweepGC -Djava.library.path=/home/ec2-user/sis-jhdf5/lib/native/jhdf5/amd64-Linux" sbt console

import songs._
val inputDir="/home/ec2-user/MillionSongSubset/data"
val files: Vector[String] = Files.getPaths(inputDir)
val f = HDF5.open(files.head)
f.toOption.map(ReadSong.readSongs).flatMap(_.toOption).map(SongML.extractFeatures)
```



# Run the application in Spark Shell
```
sudo service spark-master start
spark-shell --jars /home/ec2-user/millionsongs/target/scala-2.10/songs.jar
```


# Submit application to run on cluster
```
spark-submit --class "songs.Main" --master local[6] /home/ec2-user/millionsongs/target/scala-2.10/songs.jar
```


