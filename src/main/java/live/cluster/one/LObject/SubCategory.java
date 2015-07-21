package live.cluster.one.LObject;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SubCategory{
    private String sub_cat_id;
    private String sub_cat_name;
    private List<String> productList = new ArrayList<String>();

    //Getters and Setters
    public String getSub_cat_id() {
        return sub_cat_id;
    }

    public void setSub_cat_id(String sub_cat_id) {
        this.sub_cat_id = sub_cat_id;
    }

    public String getSub_cat_name() {
        return sub_cat_name;
    }

    public void setSub_cat_name(String sub_cat_name) {
        this.sub_cat_name = sub_cat_name;
    }

    public void addProduct(String productId){
        if(!productList.contains(productId)){
            productList.add(productId);
        }
    }

    public JSONObject getJSON(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sub_cat_id", sub_cat_id);
        jsonObject.put("product_count",productList.size());
        return jsonObject;
    }
}