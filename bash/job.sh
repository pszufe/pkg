#!/bin/bash

SGE_TASK_ID=$1

foo="$(printf "%06d" $SGE_TASK_ID)"

echo $foo

package='experiments'
clazz='Experiments_N5_W10_SKG_AKG_OCBA_AOCBA'
filename="/home/ubuntu/pkg/res/${clazz}_${foo}_${HOSTNAME}_$(date '+%Y-%m-%d_%H%M%S').log.txt"
datefolder=$(date '+%Y-%m-%d')
echo $filename

java -server -Xmx1500M -cp "/home/ubuntu/pkg/app/*" ${package}.${clazz} $SGE_TASK_ID &>$filename

gzip $filename
aws s3 --region us-east-1 cp ${filename}.gz s3://pszufe-simres/PKG/res/${clazz}/${datefolder}/

