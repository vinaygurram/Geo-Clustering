package live.cluster.one.LObject;

import org.json.JSONObject;

/**
 * Created by gurramvinay on 7/9/15.
 */
public class Product {
    private String id;
    private int quantity;
    private int price;
    private String state;

    public Product(String id, int quantity, int price, String state){
        this.id = id;
        this.quantity = quantity;
        this.price = price;
        this.state = state;
    }


    //Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof Product)) return false;
        Product product = (Product) obj;
        if(product.getId()!=this.id) return false;
        return true;
    }

    public JSONObject getJSONObj(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",id);
        jsonObject.put("quantity",quantity);
        jsonObject.put("price",price);
        jsonObject.put("state",state);
        return jsonObject;
    }
}
