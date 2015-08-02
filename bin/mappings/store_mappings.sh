#!/usr/bin/env bash
ES_HOST="http://localhost:9200"
ES_INDEX_NAME="stores"
ES_INDEX_MAPPINGS="{
    \"settings\" :{
        \"number_of_shards\": 1,
        \"index.mapper.dynamic\": false
    },
    \"mappings\" :{
        \"store\" :{
            \"properties\" :{
                \"id\" : {\"type\" : \"string\"},
                \"location\" : {\"type\" : \"geo_point\"},
                \"name\" : {\"type\" : \"string\"},
                \"state\" : {\"type\" : \"boolean\"}
            }
        }
    }
}"
curl -s -XDELETE ${ES_HOST}"/"${ES_INDEX_NAME}
echo ""
curl -s -XPOST ${ES_HOST}"/"${ES_INDEX_NAME} -d "${ES_INDEX_MAPPINGS}"
echo ""
