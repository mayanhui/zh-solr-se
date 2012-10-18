#!/bin/bash

SCRIPT_DIR=$(dirname $0)
WHERE_CORES_EXIST="${SCRIPT_DIR}/../solr"

####
# Convert the name of the core to the correct format if needed
#
core_name=$1
if [[ "$core_name" != core-* ]]; then
    core_name="core-${core_name}" 
    echo "core name did not start with core-, coverted to: ${core_name}" >&2
fi

####
# Make sure the core is a valid core - is one of the existing cores
#
is_valid=0
for core in $(ls "${WHERE_CORES_EXIST}")
do
  if test "${core_name}" = "${core}"; then
    is_valid=1
    break
  fi
done

if test $is_valid -ne 1; then
  echo "core name '${core_name}' is not a valid core name" >&2
  echo "exiting" >&2
  exit 1
fi


####
# Figure out which directory to copy the new index into
#

# function to get the name of the index dir from the index.properties file
# if no index.properties file exists it returns 'index' - the default
# name of a solr index directory
#
function get_index_properties_dir_name() {
  # default name of index directory
  default_dir_name='index'
  dir_name=${default_dir_name}

  if test "x$@" != 'x'; then
    solr_data_dir="$@"
    prop_file="${solr_data_dir}/index.properties"
    if test -f $prop_file; then
      dir_name=$(awk -F= '{print $2}' $prop_file)
    fi
  else
    echo "$(basename $0):get_index_properties_file_name: The name of the core's data directory is required!" >&2
    echo 1
    return
  fi

  # If for some reason the file name is empty or does not exist
  # use the default name
  if test "x${dir_name}" = "x" -o ! -d "${solr_data_dir}/${dir_name}"; then
    dir_name=$default_dir_name
  fi
  echo $dir_name
}

SCRIPT_DIR=$(dirname $0)
DATA_DIR="${SCRIPT_DIR}/../solr/${core_name}/data"
TARGET_INDEX_DIR_NAME=$(get_index_properties_dir_name ${DATA_DIR})

if test "${TARGET_INDEX_DIR_NAME}" = "1"; then
  echo "exiting" >&2
  exit 1
fi

SOURCE_INDEX_DIR="${SCRIPT_DIR}/solr/${core_name}/data/index"
TARGET_INDEX_DIR="${SCRIPT_DIR}/../solr/${core_name}/data/${TARGET_INDEX_DIR_NAME}"


####
# Verify that the source directory exists before deleting the target data
#
if test ! -d "${SOURCE_INDEX_DIR}"; then
  echo "the source index directory '${SOURCE_INDEX_DIR}' does not exist" >&2
  echo "exiting" >&2
  exit 1
fi

####
# Verify that the source directory contains files to be copied
#
files=$(ls $SOURCE_INDEX_DIR)
if test "x$files" = "x"; then
  echo "the source index directory '${SOURCE_INDEX_DIR}' contains no files to copy." >&2
  echo "exiting" >&2
  exit 1
fi

####
# Delete the existing index
#
echo "deleting existing index directory 'rm -rf ${TARGET_INDEX_DIR}/*'"
rm -rf ${TARGET_INDEX_DIR}/*

####
# Copy the new index to the index dir
#
COPY_CMD="cp ${SOURCE_INDEX_DIR}/* ${TARGET_INDEX_DIR}"
echo "copy the new index to the index dir: ${COPY_CMD}"
if ${COPY_CMD} ;then
  echo "new index has been succesfully copied"
fi

####
# Switch to the new index using the RELOAD admin action
#
echo "reloading solr ${core_name} with the new data"
echo "curl \"http://localhost:8983/solr/admin/cores?action=RELOAD&core=${core_name}\""
curl "http://localhost:8983/solr/admin/cores?action=RELOAD&core=${core_name}"

exit 0
