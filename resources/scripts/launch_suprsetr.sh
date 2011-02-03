#!/bin/sh
#
# This is the launch script for SuprSetr
# This version of the launch script is used by the graphical installer.

# If java 1.6 is not in your path, set this to the full path to java
JAVA=java

# There should be no need to edit below here...

java -jar $INSTALL_PATH/SuprSetr.jar

#LIBDIR=$INSTALL_PATH/lib
#
## The main class
#MAIN="net.jeremybrooks.suprsetr.Main"
#
#
## build classpath
#for jar in $(ls -1 $LIBDIR); do
#  if [ -z $classpath ]; then
#    classpath="$LIBDIR/$jar"
#  else 
#    classpath="$classpath:$LIBDIR/$jar"
#  fi
#done
#
## Add current dir to classpath, so we can find the properties file
#classpath="$classpath:."
#
#
#echo "$JAVA -cp $classpath $MAIN"
#
#$JAVA -cp $classpath $MAIN
#
##
