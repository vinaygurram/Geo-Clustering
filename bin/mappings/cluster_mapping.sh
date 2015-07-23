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
                \"catalog_tree\": {
                    \"type\": \"nested\",
                    \"properties\": {
                        \"sup_cat_id\": {
                            \"type\": \"string\"
                        },
                        \"cat\": {
                            \"type\": \"nested\",
                            \"properties\": {
                                \"cat_id\": {
                                    \"type\": \"string\"
                                },
                                \"product_count\": {
                                    \"type\": \"integer\"
                                },
                                \"sub_cat\": {
                                    \"type\": \"nested\",
                                    \"properties\": {
                                        \"sub_cat_id\": {
                                            \"type\": \"string\"
                                        },
                                        \"product_count\": {
                                            \"type\": \"integer\"
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
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
                \"load\": {
                    \"type\": \"integer\"
                },
                \"rank\": {
                    \"type\": \"double\"
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
