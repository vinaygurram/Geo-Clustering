#!/usr/bin/env bash
#ES Variables
ES_URL="http://localhost:9200/live_geo_clusters"
ES_MAPPINGS="{
    \"settings\": {
        \"number_of_shards\": 1,
        \"index.mapper.dynamic\": false
    },
    \"mappings\": {
        \"geo_cluster\": {
            \"properties\": {
                \"stores\": {
                    \"properties\": {
                        \"id\": {
                            \"type\": \"integer\"
                        }
                    }
                },
                \"stores_count\": {
                    \"type\": \"integer\"
                },
                \"product_count\": {
                    \"type\": \"integer\"
                },
                \"sub_cat_count\": {
                    \"type\": \"integer\"
                },
                \"rank\": {
                    \"type\": \"double\"
                },
                \"is_online\" : {
                    \"type\" : \"boolean\"
                },
                \"is_active\" :{
                    \"type\" : \"boolean\"
                }
            }
        }
    }
}"

#check and delete the old index
CURL -s -XDELETE ${ES_URL}
echo
#create new mappings
curl -s -XPUT ${ES_URL} -H "Content-Type: application/json" -d "${ES_MAPPINGS}"
