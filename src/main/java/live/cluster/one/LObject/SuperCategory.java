package live.cluster.one.LObject;

class SuperCategory{

        private String sup_cat_id;
        private String sub_cat_name;
        private String sup_cat_image_url;
        private List<Category> catList;

        //Getters and Setters


        public String getSup_cat_id() {
            return sup_cat_id;
        }

        public void setSup_cat_id(String sup_cat_id) {
            this.sup_cat_id = sup_cat_id;
        }

        public String getSub_cat_name() {
            return sub_cat_name;
        }

        public void setSub_cat_name(String sub_cat_name) {
            this.sub_cat_name = sub_cat_name;
        }

        public String getSup_cat_image_url() {
            return sup_cat_image_url;
        }

        public void setSup_cat_image_url(String sup_cat_image_url) {
            this.sup_cat_image_url = sup_cat_image_url;
        }

        public List<Category> getCatList() {
            return catList;
        }

        public void setCatList(List<Category> catList) {
            this.catList = catList;
        }
    }