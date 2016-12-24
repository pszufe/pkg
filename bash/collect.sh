#!/bin/bash

sudo lsblk
sudo mkfs -t ext4 /dev/xvdb
mkdir res1
sudo mount /dev/xvdb res1
sudo chown ubuntu res1/
 
aws --region us-east-1 s3 cp s3://pszufe-simres/PKG/app/pkg.jar .

aws --region us-east-1 s3 cp s3://pszufe-simres/PKG/res/Experiments_N5_v1/2016-11-19 ./res1/ --recursive

java -server -cp pkg.jar visualizations.Tylda res1
java -server -cp pkg.jar visualizations.Avg res1/results.csv > averages.txt


aws --region us-east-1 s3 cp averages.txt s3://pszufe-simres/PKG/res/ags/averages2.txt
aws --region us-east-1 s3 cp res1/parameters.csv s3://pszufe-simres/PKG/res/ags/parameters2.txt