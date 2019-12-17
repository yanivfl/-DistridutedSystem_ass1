#!/bin/bash
sudo yum install wget
sudo yum -y install awscli
wget --continue --no-check-certificate -O jdk-8-linux-x64.tar.gz --header Cookie: oraclelicense=a http://download.oracle.com/otn-pub/java/jdk/8-b132/jdk-8-linux-x64.tar.gz

yum -y install java-1.8.0
echo 2 | alternatives --config java

echo "***** downloading jarsAss1.zip *****"
aws s3 cp s3://akiaj24cwsltdpfv43lqajars/jarsAss1.zip / --region us-east-1

echo "***** unzip jarsAss1.zip *****"
unzip -P YanivYuval /jarsAss1.zip

echo "***** running our program *****"
$JAR_COMMAND
