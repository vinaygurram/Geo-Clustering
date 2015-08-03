#!/usr/bin/env bash
ES_HOST="http://localhost:9200"
ES_INDEX="listing"
ES_MAPPINGS="{

    \"settings\" : {
        \"number_of_shards\" : 5
    },
    \"mappings\" : {
        \"list\" : {
            \"properties\" : {
                \"store\" : {
                    \"type\" : \"object\",
                    \"properties\" : {
                        \"id\" : {\"type\" : \"string\"},
                        \"name\" : {\"type\" : \"string\"},
                        \"state\" : {\"type\" : \"string\"},
                        \"location\" : {\"type\" : \"geo_point\"}
                    }
                },
                \"product\" : {
                    \"type\" : \"object\",
                    \"properties\" : {
                        \"id\" : {\"type\" : \"string\"},
                        \"state\" : {\"type\" : \"string\"},
                        \"sup_cat_id\" : {\"type\" : \"string\"},
                        \"sub_cat_id\" : {\"type\" : \"string\"},
                        \"cat_id\" : {\"type\" : \"string\"}
                    }
                }
            }
        }
    }
}"

curl -s -XDELETE ${ES_HOST}"/"${ES_INDEX}
echo
curl -s -XPOST ${ES_HOST}"/"${ES_INDEX} -d "${ES_MAPPINGS}"
