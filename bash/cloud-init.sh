#!/bin/bash

mkdir /home/ubuntu/pkg/
mkdir /home/ubuntu/pkg/app/
mkdir /home/ubuntu/pkg/res/
mkdir /home/ubuntu/pkg/log/

aws s3 --region us-east-1 sync s3://pszufe-simres/PKG/app/ /home/ubuntu/pkg/app/

retry=1
nextid=0
while [ $retry -eq 1 ]
do
  id=`aws dynamodb --region us-east-1 get-item --table-name cluster --key '{"type":{"S":"currentjobid"},"node":{"S":"none"}}' | jq -r '.Item.id.N'`
  nextid=$((id+1))
  retry=0
  {
    aws dynamodb --region us-east-1 put-item --table-name cluster --item '{"type":{"S":"currentjobid"},"node":{"S":"none"},"id":{"N":"'${nextid}'"}}' --condition-expression 'id=:id1' --expression-attribute-value '{":id1":{"N":"'${id}'"}}' &> /dev/null
  } || {
    echo "Will retry - with ${nextid}  I have failed"
    retry=1
  }
done

date1=$(date -u +%Y-%m-%dT%H:%M:%S)
echo "Got id! ${nextid} at ${date1}"

aws dynamodb --region us-east-1 put-item --table-name cluster --item '{"type":{"S":"jobid"},"node":{"S":"'${HOSTNAME}'"},"id":{"N":"'${nextid}'"},"date":{"S":"'${date1}'"}}'


bash /home/ubuntu/pkg/app/jobs.sh ${nextid}
