#!/bin/bash

if [ $# -lt 2 ]
then
    echo "Usage: $0 core_name data_type [index_source_file]"
    exit 1
fi

CORE_NAME=$1
if [[ "$CORE_NAME" != core-* ]]; then
	CORE_NAME="core-${CORE_NAME}"
	echo "core name did not start with core-, coverted to: ${CORE_NAME}"
fi

INDEXER_DIR="/var/zh-solr-se/solr/indexer"
DATA_DIR="${INDEXER_DIR}/solr/${CORE_NAME}/data/index"

cd ${INDEXER_DIR}

# delete the existing index if any
if [[ -d ${DATA_DIR} ]] ; then
    rm -rf ${DATA_DIR}
fi

MIN_MEM=500m
MAX_MEM=1500m

# Reduce the MAX_MEM if we're on a "solr0" box
# otherwise the indexer runs out of memory
# NOTE: the solr.sh script also has to me modified to reduce
#       its memory usage.
if hostname | grep -q 'solr0'; then
  MAX_MEM=1200m
fi

echo "Using -Xms$MIN_MEM -Xmx${MAX_MEM}"

INDEXING_CMD="java -Xms${MIN_MEM} -Xmx${MAX_MEM} -cp ./indexer.jar zh.solr.se.indexer.IndexerMain $1 $2 $3"
if ${INDEXING_CMD} ;then
    echo "Indexing for core: ${CORE_NAME} is successfully done"
    echo "deploying the new index ..."
    ./deploy_index.sh ${CORE_NAME}
    exit 0
else
    echo "Indexing for core: ${CORE_NAME} failed"
    exit 2
fi
