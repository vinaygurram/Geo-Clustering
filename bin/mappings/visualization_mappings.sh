#!/usr/bin/env bash
ES_DATA_VIS_INDEX="geo"
ES_URL="http://localhost:9200/visual_index"
ES_DATA_VIS_INDEX_GEO_MAPPINGS="{
  \"settings\" : {},
  \"mappings\" : {
        \"geo\" : {
            \"properties\" : {
                \"product_count\" : {\"type\": \"integer\"},
                \"rel_fnv_count\" : {\"type\": \"integer\"},
                \"rel_nfnv_count\" : {\"type\": \"integer\"},
                \"rank\" : {\"type\": \"double\"},
                \"geo_hash\" : {\"type\": \"geo_point\"},
                \"location\" : {\"type\" : \"geo_point\"}
            }
        }
  }
}"


#check and delete the old index
CURL -s -XDELETE ${ES_URL}
echo
#create new mappings
curl -s -XPUT ${ES_URL} -H "Content-Type: application/json" -d "${ES_DATA_VIS_INDEX_GEO_MAPPINGS}"


