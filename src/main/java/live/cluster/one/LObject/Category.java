package live.cluster.one.LObject;

class Category{
        private String cat_id;
        private String cat_name;
        private String cat_image_url;
        private List<SubCategory>  subCatList;

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
    }