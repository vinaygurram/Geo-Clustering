package reports;

import java.util.Set;

/**
 * Created by gurramvinay on 8/13/15.
 */
public class ESTempObject {
    private int subCatCount;
    private int storesCount;
    private Set<String> products;
    private int fnvCount;
    private int relNFNVCount;
    private int product_count;


    //getters and setters

    public int getSubCatCount() {
        return subCatCount;
    }

    public void setSubCatCount(int subCatCount) {
        this.subCatCount = subCatCount;
    }

    public int getStoresCount() {
        return storesCount;
    }

    public void setStoresCount(int storesCount) {
        this.storesCount = storesCount;
    }

    public Set<String> getProducts() {
        return products;
    }

    public void setProducts(Set<String> products) {
        this.products = products;
    }

    public int getFnvCount() {
        return fnvCount;
    }

    public void setFnvCount(int fnvCount) {
        this.fnvCount = fnvCount;
    }

    public int getRelNFNVCount() {
        return relNFNVCount;
    }

    public void setRelNFNVCount(int relNFNVCount) {
        this.relNFNVCount = relNFNVCount;
    }

    public int getProduct_count() {
        return product_count;
    }

    public void setProduct_count(int product_count) {
        this.product_count = product_count;
    }
}

