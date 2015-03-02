#!/bin/bash
#   Copyright (C) 2013-2014 Computer Sciences Corporation
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.


CMD=$1
PORT=$2
JAR_APP=$3
TMP_DIR=$4

if [ "x$TMP_DIR" == "x" ]; then
    TMP_DIR=/tmp/ez_zk
fi

ZK_PID=$TMP_DIR/ez_zk.pid


if [ "x$CMD" == "x" ]
then
    echo "USAGE: $0 startClean|start|stop port [jar path] [tmpDir]"
    exit 2
fi

case "`uname`" in
    CYGWIN*) cygwin=true ;;
    *) cygwin=false ;;
esac

if $cygwin
then
    # cygwin has a "kill" in the shell itself, gets confused
    KILL=/bin/kill
else
    KILL=kill
fi


case $CMD in
start|startClean)
    if [ "x$CMD" == "xstartClean" ]; then
        rm -rf $TMP_DIR
        mkdir -p $TMP_DIR/zkdata
    fi

    java -jar $JAR_APP $PORT $TMP_DIR/zkdata &> $TMP_DIR/ez_zk.log &
    pid=$!
    echo -n $! > $ZK_PID

    #wait for one second for everything to settle
    sleep 1
    
    if ps -p $pid > /dev/null; then
        #server is up
        echo " ZooKeeper server process started"
    else
        # server died
        echo " ZooKeeper server process failed"
    fi

    ;;
stop)
    if [ -r "$ZK_PID" ]; then
        pid=`cat $ZK_PID`
        $KILL -9 $pid
        rm -f $ZK_PID
    fi

    ;;
*)
    echo "Unknown command " + $CMD
    exit 2
esac
