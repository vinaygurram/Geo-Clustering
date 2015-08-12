package clusters.create.LObject;

public class BoundingBox {

    private Geopoint topLeft;
    private Geopoint botRight;

    public BoundingBox(Geopoint topLeft, Geopoint botRight){
        this.topLeft = topLeft;
        this.botRight = botRight;
    }


    //Getters and Setters

    public Geopoint getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Geopoint topLeft) {
        this.topLeft = topLeft;
    }

    public Geopoint getBotRight() {
        return botRight;
    }

    public void setBotRight(Geopoint botRight) {
        this.botRight = botRight;
    }

    @Override
    public String toString(){
        return new StringBuilder().append(topLeft).append(":").append(botRight).toString();
    }
}