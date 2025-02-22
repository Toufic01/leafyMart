package com.leafymart.Model;


/// Generate String
public class PlantModel {
    private String plantName;
    private String plantValue;
    private String plantRating;
    private String plantPeopleRates;
    private String plantSold;
    private int plantImage;

    /// Calling Constructor
    public PlantModel(String plantName, String plantValue, String plantRating, String plantPeopleRates, String plantSold, int plantImage) {
        this.plantName = plantName;
        this.plantValue = plantValue;
        this.plantRating = plantRating;
        this.plantPeopleRates = plantPeopleRates;
        this.plantSold = plantSold;
        this.plantImage = plantImage;
    }


    /// Creating Setter and Getter

    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    public String getPlantValue() {
        return plantValue;
    }

    public void setPlantValue(String plantValue) {
        this.plantValue = plantValue;
    }

    public String getPlantRating() {
        return plantRating;
    }

    public void setPlantRating(String plantRating) {
        this.plantRating = plantRating;
    }

    public String getPlantPeopleRates() {
        return plantPeopleRates;
    }

    public void setPlantPeopleRates(String plantPeopleRates) {
        this.plantPeopleRates = plantPeopleRates;
    }

    public String getPlantSold() {
        return plantSold;
    }

    public void setPlantSold(String plantSold) {
        this.plantSold = plantSold;
    }

    public int getPlantImage() {
        return plantImage;
    }

    public void setPlantImage(int plantImage) {
        this.plantImage = plantImage;
    }
}