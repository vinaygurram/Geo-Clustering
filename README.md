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

### Architecture ###

##### CRUD Logic ####

>Creation :: Data Input from Listing ----> Create Clusters ----> Store in ES
>Update/Delete :: Events received from Listing ----> Store the events in memory for 10 minutes ----> process the events ----> bulk push to ES
> Read :: Listing ---> Clusters Index ----> Server stores from Clusters ----> Listing


### Improvements for Listing ###

We can improve listing performance by caching the calls against the geo hash.
As of now clusters will produce stores, going forward we can support category level tree.

