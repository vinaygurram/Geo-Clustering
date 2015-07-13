package live.cluster.one.LObject;

import org.json.JSONObject;

/**
 * Created by gurramvinay on 7/10/15.
 */
public class PCat {
    private String id;
    private String spCat;
    private String sbCat;
    private String cat;

    public PCat(String id,String spCat, String sbCat,String cat){
        this.id = id;
        this.spCat = spCat;
        this.sbCat = sbCat;
        this.cat = cat;
    }

    //Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpCat() {
        return spCat;
    }

    public void setSpCat(String spCat) {
        this.spCat = spCat;
    }

    public String getSbCat() {
        return sbCat;
    }

    public void setSbCat(String sbCat) {
        this.sbCat = sbCat;
    }

    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public JSONObject getJSON(){
        JSONObject jsonObject = new JSONObject();
        JSONObject rJ = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("spcat",spCat);
        jsonObject.put("sbcat",sbCat);
        jsonObject.put("cat",cat);
        rJ.put("product",jsonObject);
        return rJ;
    }
}
