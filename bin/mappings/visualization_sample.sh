#!/usr/bin/env bash
ES_DATA_VIS_INDEX="geo"
ES_URL="http://localhost:9200/visual_index"
ES_DATA_VIS_INDEX_GEO_MAPPINGS="{
  \"settings\" : {},
  \"mappings\" : {
        \"geo\" : {
            \"properties\" : {
                \"name\" : {\"type\" : \"string\"},
                \"location\" : {\"type\" : \"geo_point\"},
                \"total_products\" : {\"type\": \"integer\"}
            }
        }
  }
}"


#check and delete the old index
CURL -s -XDELETE ${ES_URL}
echo
#create new mappings
curl -s -XPUT ${ES_URL} -H "Content-Type: application/json" -d "${ES_DATA_VIS_INDEX_GEO_MAPPINGS}"


