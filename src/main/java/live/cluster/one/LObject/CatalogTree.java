package live.cluster.one.LObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gurramvinay on 7/20/15.
 */
public class CatalogTree {

    List<SuperCategory> superCategories = new ArrayList<SuperCategory>();

    //Getters and Setters
    public List<SuperCategory> getSuperCategories() {
        return superCategories;
    }

    public void setSuperCategories(List<SuperCategory> superCategories) {
        this.superCategories = superCategories;
    }

    public JSONArray getJSONOArray(){
        JSONArray jsonArray = new JSONArray();
        for(SuperCategory superCategory : superCategories){
            jsonArray.put(superCategory.getJSONObject());
        }
        return jsonArray;
    }
}
