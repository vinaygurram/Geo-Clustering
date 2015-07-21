package live.cluster.one.LObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Category{
    private String cat_id;
    private String cat_name;
    private String cat_image_url;
    private List<SubCategory>  subCatList = new ArrayList<SubCategory>();
    private List<String> products = new ArrayList<String>();

    //Getters and Setters

    public String getCat_id() {
        return cat_id;
    }

    public void setCat_id(String cat_id) {
        this.cat_id = cat_id;
    }

    public String getCat_name() {
        return cat_name;
    }

    public void setCat_name(String cat_name) {
        this.cat_name = cat_name;
    }

    public String getCat_image_url() {
        return cat_image_url;
    }

    public void setCat_image_url(String cat_image_url) {
        this.cat_image_url = cat_image_url;
    }

    public List<SubCategory> getSubCatList() {
        return subCatList;
    }

    public void setSubCatList(List<SubCategory> subCatList) {
        this.subCatList = subCatList;
    }
    public void addProduct(String id){
        if(!this.products.contains(id)){
            products.add(id);
        }
    }
    public void addSubCategory(SubCategory subCategory){
        boolean is_sub_cat_exists = false;
        //double check; do not need it but still
        for(SubCategory subCategory1 : subCatList){
           if(subCategory1.getSub_cat_id().contentEquals(subCategory.getSub_cat_id())){
               is_sub_cat_exists = true;
           }
        }
        if(!is_sub_cat_exists){
            subCatList.add(subCategory);
        }
    }

    public JSONObject getJSONObject(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("cat_id",cat_id);

        if(products.size()==0){
            JSONArray jsonArray = new JSONArray();
            for(SubCategory subCategory: subCatList){
                JSONObject jsonObject1 = subCategory.getJSON();
                jsonArray.put(jsonObject1);
            }
            jsonObject.put("sub_cat",jsonArray);
        }else {
            jsonObject.put("sub_cat",new JSONArray());
        }
        return jsonObject;
    }
}