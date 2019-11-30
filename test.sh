#!/bin/bash

apt-get install wget
sudo apt install openjdk-8-jdk
sudo apt install openjdk-8-jre


aws s3 sync s3://akiaj24cwsltdpfv43lqajars/jarsAss1.zip .
unzip -P YanivYuval jarsAss1.zip 


TODO: add our jar file!!


