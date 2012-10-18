#!/bin/bash
#
# solr        This shell script takes care of starting and stopping
#               the solr subsystem (solr).
#
# chkconfig: - 64 36
# description:  Lucene Solr server.
# processname: solr
#
# Arguments:
#  --no-check
#    without this argument the script will check and exit if solr is already running
#
#  --debug
#    use this flag to start solr in debug mode (mostly more logging)
#
#  --minmem #
#    set the minimum amount of memory (in MB)
#    the # value should be an integer
#
#  --maxmem
#    set the maximum amount of memory (in MB)
#    the # value should be an integer
#
#  --use-log-properties
#    use this to explicitly use the log.properties file
#    using this file will reduce the amount of logging sent to log file
#    this is the default for production servers
#    this is ignored when stopping solr
#
#  --do-not-use-log-properties
#    use this to explicitly NOT use the log.properties file
#    using this file will increase the amount of logging sent to log file
#    this is the default for dev and qa servers
#    this is ignored when stopping solr
#

# Source function library.
test -f /etc/rc.d/init.d/functions && . /etc/rc.d/init.d/functions

# Source networking configuration.
test -f /etc/sysconfig/network && . /etc/sysconfig/network

PS=`which ps`
PS_OPTS=auxww
GREP=`which egrep`
JAVA_EXE=`which java`
#SOLR_BASE=/var/solr
SOLR_BASE=/var/zh-solr-se/solr
SOLR_HOME=$SOLR_BASE/solr
STOP_PORT=8984
STOP_KEY='some-key'

LOG=$SOLR_BASE/logs/solr.log
LOG_PROPERTIES=$SOLR_BASE/solr/log.properties
LOGGING_OPT="-Djava.util.logging.config.file=$LOG_PROPERTIES"
USE_LOG_PROPERTIES=0

MIN_MEM=768
MAX_MEM=2048

TIME_ZONE="GMT"

# Bump up the MAX_MEM if we're on a "solr2" box
# Use the log.properties in production which will reduce amount logged
# This use of a log.properties file can be overridden using either
# --use-log-properties or --do-not-use-log-properties
if hostname | grep -q '^solr2'; then
  USE_LOG_PROPERTIES=1
  MAX_MEM=8192
fi

test -f /etc/solr/solr_vars && source /etc/solr/solr_vars

CHECK_IF_RUNNING=1
DO_RESTART=0
ACTION=

DEBUG=
JAR_START="-jar start.jar"
STOP=
DEBUG_FLAG=

function is_solr_running() {
  ps=`$PS auxww| $GREP -i solr| $GREP java | $GREP solr.home  | $GREP -v grep`
  if test -z "$ps"; then
    echo 0;
  else
    echo 1;
  fi
}

function exit_if_solr_is_running() {
  running=`is_solr_running`
  
  if test "$running" = "1"; then
    echo "Solr is already running. Exiting"
    exit;
  fi
}

function exit_if_solr_is_not_running() {
  running=`is_solr_running`
  
  if test "$running" = "0"; then
    echo "Solr is not running. Exiting"
    exit;
  fi
}

function wait_for_solr_to_stop() {
  echo "Waiting for solr to shutdown"
  echo `get_log_date` "Waiting for solr to shutdown" >> $LOG
  while true; do
    running=`is_solr_running`
  
    if test "$running" = "0"; then
      echo `get_log_date` "solr has shutdown" >> $LOG
      break
    fi
    sleep 1
  done
}

function usage() {
  echo "usage: $0 start|stop"
  echo "  starts and stops solr"
  if test -n "$1"; then
    echo "   ERROR: $1"
  fi
}

function get_log_date() {
  date +'%Y-%m-%d %H:%M:%S'
}

function is_int() {
  return $(test "$@" -eq "$@" > /dev/null 2>&1);
}

while test -n "$1"; do
  case "$1" in
    '--no-check')
      CHECK_IF_RUNNING=0
      shift
      ;;

    '--debug')
      DEBUG_FLAG=$1
      DEBUG="-DDEBUG"
      shift
      ;;

    '--minmem')
      shift
      if is_int "$1"; then
        MIN_MEM=$1
      else
        echo "--minmem option value ($1) must be an integer"
        exit
      fi
      shift
      ;;


    '--maxmem')
      shift
      if is_int "$1"; then
        MAX_MEM=$1
      else
        echo "--maxmem option value ($1) must be an integer"
        exit
      fi
      shift
      ;;

    '--use-log-properties')
      shift
      USE_LOG_PROPERTIES=1
      ;;

    '--do-not-use-log-properties')
      shift
      USE_LOG_PROPERTIES=0
      ;;

    'start')
      if test "$CHECK_IF_RUNNING" = "1"; then
        exit_if_solr_is_running
      fi
      ACTION=Starting
      shift
      ;;

    'stop')
      if test "$CHECK_IF_RUNNING" = "1"; then
        exit_if_solr_is_not_running
      fi
      MIN_MEM=100
      MAX_MEM=200
      ACTION=Stopping
      STOP="--stop"
      shift
      ;;

    'restart')
      ACTION=Restart
      DO_RESTART=1
      shift
      ;;

    *)
      usage "option '$1' is unknown"
      exit
      ;;
  esac
done

if test -z "$ACTION"; then
  usage "No option passed in"
  exit
fi

if test $MAX_MEM -lt $MIN_MEM; then
  echo "Maximum memory (--maxmem) '$MAX_MEM' must be larger than minimum memory (--minmem) '$MIN_MEM'"
  exit
fi

if test "$DO_RESTART" = "1"; then
  $0 --do-not-use-log-properties --no-check --maxmem $MAX_MEM --minmem $MIN_MEM $DEBUG_FLAG stop
  wait_for_solr_to_stop
  logging_properties='--use-log-properties'
  if test "$USE_LOG_PROPERTIES" = "0"; then
    logging_properties='--do-not-use-log-properties'
  fi
  $0 $logging_properties --no-check --maxmem $MAX_MEM --minmem $MIN_MEM $DEBUG_FLAG start
  exit
fi

MIN_MEM="-Xms${MIN_MEM}M"
MAX_MEM="-Xmx${MAX_MEM}M"

if test "$USE_LOG_PROPERTIES" = "0"; then
  unset LOGGING_OPT
fi

CMD="$JAVA_EXE $LOGGING_OPT -Dsolr.solr.home=$SOLR_HOME $MIN_MEM $MAX_MEM -Duser.timezone=$TIME_ZONE -DSTOP.PORT=$STOP_PORT -DSTOP.KEY=$STOP_KEY $DEBUG $JAR_START $STOP"

cd $SOLR_BASE
echo `get_log_date` "$ACTION solr using command: $CMD" >> $LOG
echo "$ACTION solr"
exec $CMD >> $LOG 2>&1 &
