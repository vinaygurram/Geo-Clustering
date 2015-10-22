#!/bin/bash
#input parameter reading
is_exit=false;
error_message=""
user_email=vinay.gurram@olacabs.com
if [ "$#" -ne 2 ] ; then
  is_exit=true
  error_message=$(echo "Illegal number of arguments, pass env and city_codes")
elif [ -z "$1" ];then
  is_exit=true
  error_message=$(echo "Environment should not be empty")
elif [ -z "$2" ];then
  is_exit=true
  error_message=$(echo "City Code string should not be empty")
fi
if [ $is_exit == true ];then
  echo ${error_message} | mail -s "clustering error" "$user_email"
  exit;
fi

ENV=$1
city_codes=$2
city_array=$(echo ${city_codes}| tr "," "\n")

#check if mappping files and jar file are present
if [ ! -r ../../src/main/resources/mappings/geo_mappings.json ];then
  is_exit=true
  error_message=$(echo "Geo Mappings file does not exist")
 exit;
elif [ ! -r ../../src/main/resources/mappings/cluster_mappings.json ]
 then
  is_exit=true
  error_message=$(echo "Cluster Mappings file does not exist")
elif [ ! -r ../../target/clustering-1.0-SNAPSHOT.jar ]
 then
  is_exit=true
  error_message=$(echo "Clustering jar file does not exist")
fi
if [ $is_exit == true ];then
  echo ${error_message} | mail -s "clustering error" "$user_email"
  exit;
fi

#create hosts and indexes
ES_HOST=""
if [ $ENV == "dev" ]; then
  ES_HOST=localhost:9200
elif [ $ENV == "qa" ]; then
  ES_HOST=http://es.qa.olahack.in
elif [ $ENV == "staging" ]; then
  ES_HOST=http://es.olahack.in
elif [ $ENV == "prod" ]; then
  ES_HOST=http://escluster.internal.olastore.com:9200
else 
  echo "ENV variable provided is not compatible. Can be one of dev,qa,staging,prod" | mail -s "Cluster Error" "$user_email"
  exit
fi
ES_GEO_INDEX=geo_hashes
ES_CLUSTER_INDEX=geo_clusters
dt=$(date '+%m_%d_%Y');
#dt_1_b=$(date --date='1 day ago' '+%m_%d_%Y');
#dt_2_b=$(date -d "2 days ago" '+%m_%d_%Y')
dt_1_b=$(date -v -1d '+%m_%d_%Y');
dt_2_b=$(date -v -2d '+%m_%d_%Y')
ES_GEO_INDEX_tday=${ES_GEO_INDEX}_${dt}
ES_CLUSTER_INDEX_tday=${ES_CLUSTER_INDEX}_${dt}
ES_GEO_YESDAY_INDEX=${ES_GEO_INDEX}_${dt_1_b}
ES_CLUSTER_YESDAY_INDEX=${ES_CLUSTER_INDEX}_${dt_1_b}
ES_GEO_DEL_INDEXES=${ES_GEO_INDEX}_${date_2_b},${ES_CLUSTER_INDEX}_${date_2_b}

#create mappings
geo_response=$(curl -s -w "%{http_code}\\n" -XPOST ${ES_HOST}/${ES_GEO_INDEX_tday} -T ../../src/main/resources/mappings/geo_mappings.json -o /dev/null)
cluster_response=$(curl -s -w "%{http_code}\\n" -XPOST ${ES_HOST}/${ES_CLUSTER_INDEX_tday} -T ../../src/main/resources/mappings/cluster_mappings.json -o /dev/null)

#create clusters only if both the calls are successful
if [ $geo_response -eq 200 ] && [ $cluster_response -eq  200 ];then
 echo "Mappings are successfully created"
 for city in $city_array
 do
   algo_response=$(java -jar ../../target/clustering-1.0-SNAPSHOT.jar ${ENV} ${city})
   error_code=$?
   if (( $error_code )); then
      echo "algo did not run successfully. Please check the logs" | mail -s "Clustering algo error" "$user_email"
      exit;
    fi
 done
else
 echo "mappings calls failed. Geo mappings call status is $geo_response. Clusters mappings response is $cluster_response" | mail -s "Clustering algo error" "$user_email"
 exit;
fi

#changing aliases
DATA="{
      \"actions\" : [
              { \"add\" : { \"index\" : \"${ES_GEO_INDEX_tday}\", \"alias\" : \"geo_hashes\" } },
              { \"add\" : { \"index\" : \"${ES_CLUSTER_INDEX_tday}\", \"alias\" : \"geo_clusters\" } }
                  ]
}"
DATA1="{
      \"actions\" : [
              { \"remove\" : { \"index\" : \"${ES_GEO_INDEX_yday}\", \"alias\" : \"geo_hashes\" } },
              { \"remove\" : { \"index\" : \"${ES_CLUSTER_INDEX_yday}\", \"alias\" : \"geo_clusters\" } }
                  ]
}"
aliases_response=$(curl -s -w "%{http_code}\\n" -XPOST "${ES_HOST}/_aliases" -d "${DATA}" -o /dev/null)
if [ $aliases_response -eq 200 ];then
 echo "Aliases added successfully"
 aliases_response=$(curl -s -w "%{http_code}\\n" -XPOST "${ES_HOST}/_aliases" -d "${DATA1}" -o /dev/null)
 if [ $aliases_response -eq 200 ];then
  echo "Aliases deleted successfully"
 else
   echo "Aliases did not deleted. Status code is $aliases_response" | mail -s "Clusters Algo error" "$user_email"
 fi
else
 echo "Aliases did not created. Status code is $aliases_response" | mail -s "Clusters Algo error" "$user_email"
fi

delete_index_response=$(curl -s -w "%{http_code}\\n" -XDELETE "${ES_HOST}/${ES_GEO_DEL_INDEXES}" -o /dev/null)
if [ $delete_index_response -eq 200 ];then
 echo "Delteted indexes successfully"
else
 echo "Could not delete indexes. Status code is $aliases_response" | mail -s "Clustering algo error" "$user_email"
fi
echo "geo hashes created successfully" | mail -s "Clustering algo" "$user_email"

