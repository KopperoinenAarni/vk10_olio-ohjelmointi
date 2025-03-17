package com.example.vk10_olio_ohjelmoitni;

import java.util.ArrayList;

public class CarDataStorage {
    private static CarDataStorage instance;
    private String city;
    private int year;
    private ArrayList<CarData> carData = new ArrayList<>();

    private CarDataStorage() { }

    public static CarDataStorage getInstance() {
        if (instance == null) {
            instance = new CarDataStorage();
        }
        return instance;
    }

    public ArrayList<CarData> getCarData() {
        return carData;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void clearData() {
        carData.clear();
    }

    public String getCity() {
        return city;
    }

    public int getYear() {
        return year;
    }

    public void addCarData(CarData newCarData) {
        carData.add(newCarData);
    }

}
