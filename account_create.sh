#!/bin/sh
PG_NAME=`basename $0 '.sh'`
PG_HOME=$( cd $(dirname $0)/../ ; pwd -P )
LIB_HOME="${PG_HOME}/lib"
accountcreator_JAR=`ls -1 ${LIB_HOME}/namie-accountcreator-*.jar`

CONF_FILE=${PG_HOME}/${PG_NAME}.conf

# Crowler JAR filename is defined as a head to detect logback configuration file that contained.
NAM_JARS="${accountcreator_JAR}:"
for i in $(ls -1 ${LIB_HOME}/*.jar); do NAM_JARS="${NAM_JARS}${i}:"; done

if [ $# -ne 1 ]; then
  echo "ファイル名の指定が必要です。"
  exit 1
fi
CMD=$1

if [ -r "$CONF_FILE" ]; then
  . $CONF_FILE
fi

if [ -z "$JAVA_OPTS" ]; then
  JAVA_OPTS=""
fi

if [ -z "$TMP_DIR" ]; then
  TMP_DIR=${PG_HOME}/log
fi

if [ -z "$BOOTSTRAP_CLASS" ]; then
  BOOTSTRAP_CLASS=jp.fukushima.namie.town.accountcreator.AccountCreator
fi

#---------------------------------
# check duplicate execute
#---------------------------------
mkdir -p $TMP_DIR
LOCK_FILE=$TMP_DIR/.${PG_NAME}.lock
echo $LOCK_FILE

if [ -f $LOCK_FILE ]; then
    echo "$PG_NAME is already executed."
    exit 1
fi
echo $$ > $LOCK_FILE
trap 'rm -r $LOCK_FILE' 0 1 2 3 10 15

#---------------------------------
# run
#---------------------------------
java ${JAVA_OPTS} -cp $NAM_JARS ${BOOTSTRAP_CLASS} $CMD
