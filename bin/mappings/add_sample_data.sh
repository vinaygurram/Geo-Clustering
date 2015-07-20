#!/usr/bin/env bash
ES_URL="http://localhost:9200/cluster_index/hash_cluster/4"
data1="{
    \"catalog_tree\" : {

    },
    \"store_ids\" : [
        {\"id\" : 21},
        {\"id\" : 22},
        {\"id\" : 23}
    ],
    \"id\" : \"sdfsdfds\",
    \"distance\" : 2.0
}"
data2="{
    \"catalog_tree\" : [
        {\"sup_cat\" : {
           \"image_url\" : \"ads\",
           \"cat\" : [],
           \"name\" : \"sup_cat1\",
           \"id\"  :3232,
           \"leaf\" : false
        }
    ],
    \"store_ids\" : [
        {\"id\" : 21},
        {\"id\" : 22},
        {\"id\" : 23}
    ],
    \"id\" : \"sdfsdfds\",
    \"distance\" : 2.0
}"


data3="{
    \"catalog_tree\" : [
        {
        \"cat\" : [
        ],
        \"sup_cat_id\" : 232,
        \"sup_cat_name\" : \"dsfsd\",
        \"sup_cat_image_url\" : \"image_dsfsd\",
        \"sup_cat_leaf\" : true
        },
        {
        \"cat\" : [
        ],
        \"sup_cat_id\" : 234,
        \"sup_cat_name\" : \"dsfsd1\",
        \"sup_cat_image_url\" : \"image_dsfsd1\",
        \"sup_cat_leaf\" : false
        }
    ],
    \"store_ids\" : [
        {\"id\" : 21},
        {\"id\" : 22},
        {\"id\" : 23}
    ],
    \"id\" : \"sdfsdfds\",
    \"distance\" : 2.0,
    \"rank\" : 2.0
}"

data4="{
    \"catalog_tree\" : [
        {
        \"cat\" : [
        {
            \"cat_id\" : \"sda2312\",
            \"cat_name\" : \"sda2312\",
            \"cat_leaf\" : \"sda2312\",
            \"cat_image_url\" : \"sda231ew232\",
            \"sub_cat\" : [
            {
                \"sub_cat_id\" : \"dsfasd\",
                \"sub_cat_name\" : \"d3232sfasd\",
                \"sub_cat_image_url\" : \"dsfasd\",
                \"product_count\" : 340
            },
            {
                \"sub_cat_id\" : \"dsfasd\",
                \"sub_cat_name\" : \"d3232sfasd\",
                \"sub_cat_image_url\" : \"dsfasd\",
                \"product_count\" : 340
            }
            ]
        },
        {
        }
        ],
        \"sup_cat_id\" : 232,
        \"sup_cat_name\" : \"dsfsd\",
        \"sup_cat_image_url\" : \"image_dsfsd\",
        \"sup_cat_leaf\" : true
        },
        {
        \"cat\" : [
        ],
        \"sup_cat_id\" : 234,
        \"sup_cat_name\" : \"dsfsd1\",
        \"sup_cat_image_url\" : \"image_dsfsd1\",
        \"sup_cat_leaf\" : false
        }
    ],
    \"store_ids\" : [
        {\"id\" : 21},
        {\"id\" : 22},
        {\"id\" : 23}
    ],
    \"id\" : \"sdfsdfds\",
    \"distance\" : 2.0,
    \"rank\" : 2.0
}"

echo "${data1}"
result=$(curl -XPUT -s ${ES_URL}  -H "Content-Type: application/json" -d "${data4}")
echo ${result}

