package com.olastore.listing.clustering.geo;

/**
 * Bounding box class to define areas ex: city,zone,state ...
 */
public class BoundingBox {

  private Geopoint topLeft;
  private Geopoint botRight;

  public BoundingBox(Geopoint topLeft, Geopoint botRight) {
    this.topLeft = topLeft;
    this.botRight = botRight;
  }

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
  public String toString() {
    return new StringBuilder().append(topLeft).append(":").append(botRight).toString();
  }
}