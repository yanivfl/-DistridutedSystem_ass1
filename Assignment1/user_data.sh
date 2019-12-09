#!/bin/bash
sudo yum install wget
wget --continue --no-check-certificate -O jdk-8-linux-x64.tar.gz --header Cookie: oraclelicense=a http://download.oracle.com/otn-pub/java/jdk/8-b132/jdk-8-linux-x64.tar.gz



aws s3 sync s3://akiaj24cwsltdpfv43lqajars/jarsAss1.zip