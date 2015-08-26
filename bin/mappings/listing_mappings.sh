#!/usr/bin/env bash
ES_HOST="http://localhost:9200"
ES_INDEX="listing"
ES_MAPPINGS="{
    \"aliases\": {},
    \"mappings\": {
      \"list\": {
        \"properties\": {
          \"product_details\": {
            \"properties\": {
              \"available\": {
                \"type\": \"boolean\"
              },
              \"brand\": {
                \"type\": \"string\"
              },
              \"category_id\": {
                \"type\": \"long\"
              },
              \"category_name\": {
                \"type\": \"string\"
              },
              \"ff_tag\": {
                \"type\": \"string\"
              },
              \"group_id\": {
                \"type\": \"string\"
              },
              \"id\": {
                \"type\": \"string\"
              },
              \"name\": {
                \"type\": \"string\"
              },
              \"status\": {
                \"type\": \"string\"
              },
              \"sub_category_id\": {
                \"type\": \"string\"
              },
              \"sub_category_name\": {
                \"type\": \"string\"
              },
              \"sup_category_id\": {
                \"type\": \"long\"
              },
              \"sup_category_name\": {
                \"type\": \"string\"
              },
              \"variant_order\": {
                \"type\": \"long\"
              },
              \"vertical_id\": {
                \"type\": \"string\"
              }
            }
          },
          \"store_details\": {
            \"properties\": {
              \"id\": {
                \"type\": \"long\"
              },
              \"location\": {
                \"type\": \"geo_point\"
              },
              \"state\": {
                \"type\": \"string\"
              }
            }
          }
        }
      }
    },
    \"settings\": {
      \"index\": {
        \"number_of_replicas\": \"1\",
        \"analysis\": {
          \"analyzer\": {
            \"default\": {
              \"type\": \"keyword\"
            }
          }
        },
        \"number_of_shards\": \"5\"
      }
    },
    \"warmers\": {}
  }"
curl -s -XDELETE ${ES_HOST}"/"${ES_INDEX}
echo
curl -s -XPOST ${ES_HOST}"/"${ES_INDEX} -d "${ES_MAPPINGS}"
