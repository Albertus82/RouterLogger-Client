#!/bin/sh
PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
PRGDIR=`dirname "$PRG"`
if [ "$JAVA_HOME" != "" ]
  then "$JAVA_HOME/bin/java" -Xms8m -Xmx32m -classpath "$PRGDIR/routerlogger-client.jar:$PRGDIR/lib/*" it.albertus.routerlogger.client.RouterLoggerClient
  else java -Xms8m -Xmx32m -classpath "$PRGDIR/routerlogger-client.jar:$PRGDIR/lib/*" it.albertus.routerlogger.client.RouterLoggerClient
fi
