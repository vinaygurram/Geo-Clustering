package clusters.create.LObject;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by gurramvinay on 8/10/15.
 */
public class ESShop {
    private String name;
    private String id;
    private Geopoint location;

    //Cat count right now;;
    private List<String> catList = new ArrayList<String>();
    private List<String> productIDList = new ArrayList<String>();
    private int catCount ;
    private int prodCount;
    private boolean fnv= false;


    public ESShop(String name,JSONArray pids, String id, Geopoint location,ConcurrentHashMap<String,List<String>> map){
        this.name = name;

        for(int i=0;i<pids.length();i++){
            productIDList.add(pids.getString(i));
        }
        this.prodCount = productIDList.size();
        this.id = id;
        this.location = location;
        createCatList(map);
    }



    //Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Geopoint getLocation() {
        return location;
    }

    public void setLocation(Geopoint location) {
        this.location = location;
    }

    public List<String> getCatList() {
        return catList;
    }

    public void setCatList(List<String> catList) {
        this.catList = catList;
    }

    public List<String> getProductIDList() {
        return productIDList;
    }

    public void setProductIDList(List<String> productIDList) {
        this.productIDList = productIDList;
    }

    public int getCatCount() {
        return catCount;
    }

    public void setCatCount(int catCount) {
        this.catCount = catCount;
    }

    public int getProdCount() {
        return prodCount;
    }

    public void setProdCount(int prodCount) {
        this.prodCount = prodCount;
    }

    synchronized public void createCatList(ConcurrentHashMap<String,List<String>> map){
        for(String id: productIDList){
            if(map.containsKey(id)){
                List<String> list = map.get(id);
                if(list.get(2).isEmpty() || list.get(2).contentEquals("")){
                }else {
                    if(!catList.contains(list.get(2))){
                        catList.add(list.get(2));
                    }
                }
            }

        }
        catCount = catList.size();
    }

    public boolean isFnv() {
        return fnv;
    }

    public void setFnv(boolean fnv) {
        this.fnv = fnv;
    }

    @Override
    public ESShop clone(){
        return this;
    }
}
