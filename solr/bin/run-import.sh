#!/bin/bash

#
# This script will do the SOLR data import for $SOLR_CORE
# See: http://wiki.apache.org/solr/DataImportHandler for more detail but this is the command part (as of 03/20/2010)
#
# The handler exposes all its API as http requests.  The following are the
# possible operations:
#
# full-import: Full Import operation can be started by hitting the
#   URL http://<host>:<port>/solr/dataimport?command=full-import
#
#  - This operation will be started in a new thread and the status attribute in
#    the response should be shown busy now.
#
#  - The operation may take some time depending on size of dataset.
#
#  - When full-import command is executed, it stores the start time of the operation
#    in a file located at conf/dataimport.properties
#
#  - This stored timestamp is used when a delta-import operation is executed.
#
#  - Queries to Solr are not blocked during full-imports.
#
#  It takes in extra parameters
#
#    * entity: Name of an entity directly under the <document> tag. Use this to
#      execute one or more entities selectively. Multiple 'entity' parameters can
#      be passed on to run multiple entities at once. If nothing is passed , all
#      entities are executed
#
#    * clean: (default 'true'). Tells whether to clean up the index before the
#      indexing is started
#
#    * commit: (default 'true'). Tells whether to commit after the operation
#
#    * optimize: (default 'true'). Tells whether to optimize after the operation
#
#    * debug: (default false). Runs in debug mode.  It is used by the interactive
#      development mode (see here: http://wiki.apache.org/solr/DataImportHandler#interactive)
#
#      - Please note that in debug mode, documents are never committed
#        automatically. If you want to run debug mode and commit the results too,
#        add 'commit=true' as a request parameter.
#
# delta-import: For incremental imports and change detection run the command
#   http://<host>:<port>/solr/dataimport?command=delta-import.  It supports the
#   same clean, commit, optimize and debug parameters as full-import command.
#
# status: gives an elaborate statistics on no: of docs created, deleted, queries
#   run, rows fetched , status etc To know the status of the current command,
#   hit the URL http://<host>:<port>/solr/dataimport
#
# reload-config: If the data-config is changed and you wish to reload the file
#   without restarting Solr. run the command
#   http://<host>:<port>/solr/dataimport?command=reload-config
#
# abort: Abort an ongoing operation by hitting the url
#   http://<host>:<port>/solr/dataimport?command=abort
#
#
# Arguments to this script:
#  * Which core to launch (e.g., qa, local, edu, edu-articles)
#  * REQIRED
#
#      -core <VALID CORE>
#
#  * Which operation to perform (e.g., full-import)
#  * OPTIONAL
#  * Deafult value: full-import
#
#      -operation <VALID OPERATION>
#
#  * Commit
#  * OPTIONAL
#  * Deafult value: commit
#
#      -commit
#      -no-commit
#
#  * Debug
#  * OPTIONAL
#  * Deafult value: no-debug
#
#      -debug
#      -no-debug
#
#  * Clean
#  * OPTIONAL
#  * Deafult value: clean
#
#      -clean
#      -no-clean
#
#  * Host name where solr is running
#  * OPTIONAL
#  * Deafult value: localhost
#
#      -host <VALID HOST OR IP>
#
#  * Port on which solr is running
#  * OPTIONAL
#  * Deafult value: 8983
#
#      -port <VALID SOLR PORT>
#
#######
# Default values
#
SOLR_DIR=/var/zh-solr-se/solr

# SOLR core: this is the value that appears to the right of "core-"
# (e.g., for core-qa the $SOLR_CORE is qa)
# This value is also used to create the path to the log file (LOG)
# as well as the core that appears in the url.
#
SOLR_CORE=''

# Operation to perform
#
SOLR_OPERATION=full-import

# Should commit be done after operation? 
#
SOLR_COMMIT=true

# Should debug be turned on?
#
SOLR_DEBUG=false

# Should index be cleaned up before indexing?
#
SOLR_CLEAN=true

# Host and Port to connect to (where is the SOLR server running?)
#
SOLR_HOST=localhost
SOLR_PORT=8983


#######
# Shouldn't have to touch any thing below here as far as default configuration
# is concerned
#
THIS_SCRIPT=$(basename $0)

while test -n "$1"; do
  case "$1" in
   '-core')
      # pop off -core argument
      shift
      if test -n "$1"; then
        SOLR_CORE=$1
      else
        echo "${THIS_SCRIPT}: -core option must have a value (e.g., qa, local, edu)"
        exit
      fi
      # pop off value to -core argument
      shift
      ;;

   '-operation')
      # pop off -operation argument
      shift
      if test -n "$1"; then
        SOLR_OPERATION=$1
      else
        echo "${THIS_SCRIPT}: -operation option must have a value (e.g., full-import)"
        exit
      fi
      # pop off value to -operation argument
      shift
      ;;

   '-no-commit')
      # pop off -no-commit argument
      shift
      SOLR_COMMIT=false
      ;;

   '-commit')
      # pop off -commit argument
      shift
      SOLR_COMMIT=true
      ;;

   '-no-debug')
      # pop off -no-debug argument
      shift
      SOLR_DEBUG=false
      ;;

   '-debug')
      # pop off -debug argument
      shift
      SOLR_DEBUG=true
      ;;

   '-no-clean')
      # pop off -no-clean argument
      shift
      SOLR_CLEAN=false
      ;;

   '-clean')
      # pop off -clean argument
      shift
      SOLR_CLEAN=true
      ;;

   '-host')
      # pop off -host argument
      shift
      if test -n "$1"; then
        SOLR_HOST=$1
      else
        echo "${THIS_SCRIPT}: -host option must have a value (e.g., localhost)"
        exit
      fi
      # pop off value to -host argument
      shift
      ;;

   '-port')
      # pop off -port argument
      shift
      if test -n "$1"; then
        SOLR_PORT=$1
      else
        echo "${THIS_SCRIPT}: -port option must have a value (e.g., 8983)"
        exit
      fi
      # pop off value to -port argument
      shift
      ;;

    *)
      echo "${THIS_SCRIPT}: option '$1' is unknown"
      exit
      ;;
  esac
done

# SOLR_CORE and SOLR_OPERATION are required
if test -z "$SOLR_CORE"; then
  echo "usage: $(basename $0) -core <CORE>"
  echo "         -core argument with a value are required"
  exit
fi
if test -z "$SOLR_OPERATION"; then
  echo "usage: $(basename $0) -operation <OPERATION>"
  echo "         -operation argument with a value are required"
  exit
fi

# Location of log file
# Note: If the scripts are set up correctly this should not have to change
#
LOG=${SOLR_DIR}/logs/solr-${SOLR_OPERATION}-${SOLR_CORE}.log

CURRENT_DATE=$(date +"%Y-%m-%d %H:%M:%S")


echo "$THIS_SCRIPT is not running"
echo -n > $LOG
echo "Starting: $CURRENT_DATE" | tee -a $LOG
echo '-----------------------------' | tee -a $LOG
echo "using SOLR_CORE: $SOLR_CORE" | tee -a $LOG
echo "using SOLR_OPERATION: $SOLR_OPERATION" | tee -a $LOG
echo "using SOLR_COMMIT: $SOLR_COMMIT" | tee -a $LOG
echo "using SOLR_DEBUG: $SOLR_DEBUG" | tee -a $LOG
echo "using SOLR_CLEAN: $SOLR_CLEAN" | tee -a $LOG
echo "using SOLR_HOST: $SOLR_HOST" | tee -a $LOG
echo "using SOLR_PORT: $SOLR_PORT" | tee -a $LOG

URL="http://${SOLR_HOST}:${SOLR_PORT}/solr/core-${SOLR_CORE}/dataimport?command=${SOLR_OPERATION}&commit=${SOLR_COMMIT}&debug=${SOLR_DEBUG}&clean=${SOLR_CLEAN}"
CURL=`which curl`

echo | tee -a $LOG
echo "running: ${CURL} ${URL}" | tee -a $LOG
echo | tee -a $LOG

$CURL "$URL" >> $LOG 2>&1
echo '-----------------------------' | tee -a $LOG 2>&1
exit
