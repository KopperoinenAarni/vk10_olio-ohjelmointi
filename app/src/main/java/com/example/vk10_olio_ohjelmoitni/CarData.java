package com.example.vk10_olio_ohjelmoitni;

public class CarData {
    private String type;
    private int amount;

    public CarData(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}