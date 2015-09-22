#!/usr/bin/env bash

ES_HOST="http://localhost:9200"
INDEX_NAME="geo_hash_test"
INDEX_TYPE="hash_type"
INDEX_MAPPINS="{
    \"settings\": {
        \"index\": {
            \"number_of_shards\": 1,
            \"index.mapper.dynamic\": false
        }
    },
    \"mappings\": {
        \"hash_type\": {
            \"properties\": {
                \"id\": {
                    \"type\": \"string\"
                },
                \"clusters\": {
                    \"type\": \"nested\",
                    \"properties\": {
                        \"cluster_id\": {
                            \"type\": \"string\"
                        },
                        \"distance\": {
                            \"type\": \"double\"
                        }
                    }
                },
                \"clusters_count\" : {
                    \"type\" : \"integer\"
                }
            }
        }
    }
}"
SAMPLE_DATA="{
    \"id\" : \"123\",
    \"product_count\" :\"1\",
    \"sub_cat_count\"  : \"5\",
    \"clusters\" :[
        {
            \"cluster_id\" : \"23432\",
            \"distance\" : \"5.789\",
            \"state\" : \"false\"
        }
    ]
}"

SAMPLE_DATA1="{
    \"id\" : \"124\",
    \"product_count\" :\"1\",
    \"sub_cat_count\"  : \"5\",
    \"clusters\" :[
        {
            \"cluster_id\" : \"23432\",
            \"distance\" : \"5.789\",
            \"state\" : \"false\"
        },
        {
            \"cluster_id\" : \"23431\",
            \"distance\" : \"5.782\",
            \"state\" : \"true\"
        }
    ]
}"

PARTIAL_DATA="{
    \"doc\" : {
    \"clusters\": [
       {
         \"cluster_id\" : \"sdfsd\",
         \"distance\" : \"6.58\",
         \"state\" : \"true\"
       }
    ]}
}"
PARTIAL_DATA1="{
    \"script\" : \"ctx._source.clusters += obj\",
    \"params\" : {
        \"obj\" : {
                \"cluster_id\" : \"dfsadds\",
                \"distance\" : \"2.32\",
                \"state\" : \"false\"
        }
    }
}"
PARTIAL_DATA2="{
    \"script\" : \"ctx._source.clusters += obj\",
    \"params\" : {
        \"obj\" : {
                \"cluster_id\" : \"654551\",
                \"distance\" : \"2.31\",
                \"state\" : \"true\"
        }
    }
}"

#DELETE if exists
curl -s -XDELETE ${ES_HOST}"/"${INDEX_NAME}
echo ""

#Create Mappings
curl -s -XPOST ${ES_HOST}"/"${INDEX_NAME} -d "${INDEX_MAPPINS}"
echo ""

#add sample data
#curl -s -XPOST ${ES_HOST}"/"${INDEX_NAME}"/"${INDEX_TYPE} -d "${SAMPLE_DATA}"
echo ""
#curl -s -XPOST ${ES_HOST}"/"${INDEX_NAME}"/"${INDEX_TYPE} -d "${SAMPLE_DATA1}"
echo ""

#add partial update
#curl -s -XPOST ${ES_HOST}"/"${INDEX_NAME}"/"${INDEX_TYPE}"/AU66fv0PHW5Wh-tZq_24/_update" -d "${PARTIAL_DATA2}"

#reset data
#curl -s -XPOST ${ES_HOST}"/"${INDEX_NAME}"/"${INDEX_TYPE}"/AU66fv0PHW5Wh-tZq_24" -d "${SAMPLE_DATA}"
