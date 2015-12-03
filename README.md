## Geo Hash based Clustering Algorithm ##

### Introduction ###

The Objective is to show maximum listing to the consumer keeping the constraints.
It will solve the problem by defining larger sample space and using path distances.
This new algorithm will decrease the total travelling distance for the shipper.
It can improve fulfillment by minimizing the number of stores shown to the customer.
For a particular location, most of the data is pre computed.

### Geo Hash ###

Geo Hash represents rectangular area in the map.  
For more information you click [GeoHash](https://en.wikipedia.org/wiki/Geohash)

### How it Works###

Clustering algorithm computes the listing for small areas. It will divide the map into geo hashes. 
For each geo hash it will calculate the sample space. In our case we search for shops within 6kms distance.
It will create paths for that geo hashes. Paths are filtered using constraints like maximum distance.
Once we figure out the valid paths, we score the paths based on the constraints provided by the business.
We server the best path based on score.

##### CRUD Logic ####

>Creation :: Data Input from Listing ----> Create Clusters ----> Store in ES
>Update/Delete :: Events received from Listing ----> Store the events in memory for 10 minutes ----> process the events ----> bulk push to ES
> Read :: Listing ---> Clusters Index ----> Server stores from Clusters ----> Listing

### Improvements for Listing ###

We can improve listing performance by caching the calls against the geo hash.
As of now clusters will produce stores, going forward we can support category level tree.

### Installation ###
* Prerequisites : elasticsearch,maven, git 
* clone the repo
* create a jar using ```mvn clean install```
* copy the jar to listing service *$LISTING_SERVICE/bin/clusters*
* Go the the above folder and run the jar with the following commands 
```java -jar clustering-1.0-SNAPSHOT.jar dev ban,ggn,hyd```
this will run the jar in dev environment for the cities ban,ggn,hyd
* Wait for it to finish

### Production Deployment ###
* Checking out new jar
	* checkout release branch 
	* create jar using the following command. ```mvn clean install```
	* copy the jar file to *$LISTING_SERVICE/bin/clusters* on listing001
* Adding the entry in cron
	* add the following entry ```java -jar /usr/share/ola/listing-service/bin/clusters/clustering-1.0-SNAPSHOT.jar prod ban,ggn,hyd```






