#!/bin/bash

#
# Original command before this was rewritten:
#
#  curl http://localhost:8983/solr/core-chinese/dataimport?command=full-import&commit=true&debug=true&clean=true
#
# This script will run the /var/solr/run-import.sh script which will do the
# actual import.  The reason for this script is to check to see if the launch
# script is already running so a second instance is not run.
#
#######

# SOLR core: this is the value that appears to the right of "core-"
# (e.g., for core-qa the $SOLR_CORE is qa)
# This value is also used to create the path to the log file (LOG)
# as well as the core that appears in the url.
#
SOLR_CORE=chinese

# Operation to perform
#
SOLR_OPERATION=full-import

# Should commit be done after operation? 
# To turn this on use: commit
# To turn this off use: no-commit
#
SOLR_COMMIT=commit

# Should debug be turned on?
# To turn this on use: debug
# To turn this off use: no-debug
#
SOLR_DEBUG=debug

# Should index be cleaned up before indexing?
# To turn this on use: clean
# To turn this off use: no-clean
#
SOLR_CLEAN=clean

# Host and Port to connect to (where is the SOLR server running?)
#
SOLR_HOST=localhost
SOLR_PORT=8983

#######
# Shouldn't have to touch any thing below here as far as configuration is concerned
#
#######
SOLR_DIR=/var/solr
SOLR_BIN_DIR=${SOLR_DIR}/bin
IMPORT_SCRIPT_NAME=run-import.sh
IMPORT_SCRIPT="${SOLR_BIN_DIR}/${IMPORT_SCRIPT_NAME}"

PS=`which ps`
GREP=`which egrep`
STARTED=''
DURATION=''
PID=''
PS_CMD="${PS} auxww | ${GREP} bash | ${GREP} ${IMPORT_SCRIPT_NAME} | ${GREP} ${SOLR_CORE} | ${GREP} -v grep"
#echo $PS_CMD
PS_OUTPUT=`${PS} auxww | ${GREP} bash | ${GREP} ${IMPORT_SCRIPT_NAME} | ${GREP} ${SOLR_CORE} | ${GREP} -v grep`

PID=$(echo $PS_OUTPUT | awk '{print $2}')
STARTED=$(echo $PS_OUTPUT | awk '{print $9}')
DURATION=$(echo $PS_OUTPUT | awk '{print $10}')

if test -z "$PID"
then
  CMD="${IMPORT_SCRIPT} -core ${SOLR_CORE} -operation ${SOLR_OPERATION} -${SOLR_COMMIT} -${SOLR_DEBUG} -${SOLR_CLEAN} -host ${SOLR_HOST} -port ${SOLR_PORT}"
  #echo $CMD
  $CMD
  exit
else
  echo "$IMPORT_SCRIPT_NAME is already running (pid: $PID  started: $STARTED  duration: $DURATION)"
  echo exiting
  exit
fi

