#!/bin/bash

nproc=`nproc`

logfile="/home/ubuntu/pkg/log/${HOSTNAME}_$(date '+%Y-%m-%d_%H%M%S').log.txt"

jobid=$1

start=$((jobid*5000))
end=$((start+5000-1))

nohup seq $start $end | xargs --max-args=1 --max-procs=$nproc bash /home/ubuntu/pkg/app/job.sh &>> $logfile &
