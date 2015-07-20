#!/usr/bin/env bash
#ES Variables
ES_URL="http://localhost:9200/cluster_index"
ES_MAPPINGS="{
    \"settings\": {
        \"number_of_shards\": 1,
        \"index.mapper.dynamic\": false
    },
    \"mappings\": {
        \"hash_cluster\": {
            \"properties\": {
                \"id\": {
                    \"type\": \"string\"
                },
                \"catalog_tree\": {
                    \"type\": \"nested\",
                    \"properties\": {
                        \"sup_cat_id\": {
                            \"type\": \"string\"
                        },
                        \"sup_cat_name\": {
                            \"type\": \"string\"
                        },
                        \"sup_cat_image_url\": {
                            \"type\": \"string\"
                        },
                        \"sup_cat_leaf\": {
                            \"type\": \"boolean\"
                        },
                        \"cat\": {
                            \"type\": \"nested\",
                            \"properties\": {
                                \"cat_id\": {
                                    \"type\": \"string\"
                                },
                                \"cat_name\": {
                                    \"type\": \"string\"
                                },
                                \"image_url\": {
                                    \"type\": \"string\"
                                },
                                \"leaf\": {
                                    \"type\": \"boolean\"
                                },
                                \"sub_cat\": {
                                    \"type\": \"nested\",
                                    \"properties\": {
                                        \"id\": {
                                            \"type\": \"string\"
                                        },
                                        \"name\": {
                                            \"type\": \"string\"
                                        },
                                        \"image_url\": {
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
                \"store_ids\": {
                    \"properties\": {
                        \"id\": {
                            \"type\": \"integer\"
                        }
                    }
                },
                \"distance\": {
                    \"type\": \"double\"
                },
                \"rank\": {
                    \"type\": \"double\"
                }
            }
        }
    }
}"

#check and delete the old index
CURL -XDELETE ${ES_URL}

#create new mappings
var=$(curl -XPUT ${ES_URL} -H "Content-Type: application/json" -d "${ES_MAPPINGS}")
echo " result is ${var}"
