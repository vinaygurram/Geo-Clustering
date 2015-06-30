package gridbase;

import java.util.Arrays;
import java.util.List;

/**
 * Created by gurramvinay on 6/25/15.
 */
public class GeoCluster {
    private long id;
    private List<ESShop> shopList;
    private String name;





    //Getter and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<ESShop> getShopList() {
        return shopList;
    }

    public void setShopList(List<ESShop> shopList) {
        this.shopList = shopList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    //Common Methods
    @Override
    public String toString(){
        return new StringBuilder().append(name).append(" : ").append(Arrays.deepToString(shopList.toArray())).append(" id: ").append(id).toString();
    }
    @Override
    public boolean equals(Object O){
        if(!(O instanceof GeoCluster)) return false;
        GeoCluster gc = (GeoCluster)O;
        if(gc.getId()!=id) return false;
        return true;
    }


}
