GEO BASED CLUSTERING ::

Requirements :

  Max number of shops == 4;

  Min number of products = 90%;

  Distance for the cluster should be minimum



Cluster properties :
   Shop list
   OrderCount (Total number of orders for each shop)
   Seller Rating (Avg rating of all the shops)
 

Clustering Strategy :

Distance Calculation :: (Shortest Path)
   Distance from a point to existing cluster while choosing the nearest cluster

 Lets say a cluster is formed with points 1 . . .n;

  Total Distance  = Min(di+Si) for i=1..n 

  di —> distance from ith point to Geohash Cell

  Si —> Shortest distance covering all the points in the cluster ending with point i


Fav Factor ::
   Factor to determine the favoured cluster

  F= (coverage) + distance 


Get Final cluster with the given cluster:: (DFS)

 1.Get nearest point to cluster and add it to existing cluster and form cluster
 2.Get nearest point to cluster that is not present in first cluster and form a cluster
 3.get nearest point to cluster that is not present in first two cluster and form a cluster
 4.Choose the the cluster with good Fav Factor

Cluster Terminating  conditions ::

 1.Cluster reached 2 shops and got 90% coverage —End and return true
 2.Cluster got 90% coverage within 3 shop —End and return true
 3.Cluster reached 4 shops and got 85% coverage — End and return true
 4.Cluster reached 4 shops and did not reach criteria —End and return false

Cluster staring point  :

   choose the nearest point;;

Method ::
 1.Form a cluster with nearest point
 2.Get cluster with the formed cluster




/**
Add consumers as soon as possible
discuss the plan tomorrow
complete it  by weekend
*/
