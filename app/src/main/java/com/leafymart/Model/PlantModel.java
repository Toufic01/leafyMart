package com.leafymart.Model;

import java.io.Serializable;

public class PlantModel implements Serializable {
    private int id;
    private String name;
    private double price;
    private String category;
    private String imageUrl;
    private String description;
    private double rating;
    private int peopleRates;
    private int sold;
    private boolean isFavorite;

    // New fields for cart integration
    private int quantity;
    private int cartItemId;

    private int favoriteId;

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }


    public PlantModel(int id, String name, double price, String category,
                      String imageUrl, String description, double rating,
                      int peopleRates, int sold) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.description = description;
        this.rating = rating;
        this.peopleRates = peopleRates;
        this.sold = sold;
        this.isFavorite = false;
    }

    public PlantModel(int id, String name, double price, double rating,
                      int peopleRates, int sold, String imageUrl) {
        this(id, name, price, "", imageUrl, "", rating, peopleRates, sold);
    }

    public PlantModel(int productId, String name, double price, String category,
                      String imageUrl, String description, double rating, int sold) {
        this(productId, name, price, category, imageUrl, description, rating, 0, sold);
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public double getRating() { return rating; }
    public int getPeopleRates() { return peopleRates; }
    public int getSold() { return sold; }
    public boolean isFavorite() { return isFavorite; }
    public int getQuantity() { return quantity; }
    public int getCartItemId() { return cartItemId; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setCategory(String category) { this.category = category; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setRating(double rating) { this.rating = rating; }
    public void setPeopleRates(int peopleRates) { this.peopleRates = peopleRates; }
    public void setSold(int sold) { this.sold = sold; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    @Override
    public String toString() {
        return "PlantModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category='" + category + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", description='" + description + '\'' +
                ", rating=" + rating +
                ", peopleRates=" + peopleRates +
                ", sold=" + sold +
                ", isFavorite=" + isFavorite +
                ", quantity=" + quantity +
                ", cartItemId=" + cartItemId +
                '}';
    }
}
