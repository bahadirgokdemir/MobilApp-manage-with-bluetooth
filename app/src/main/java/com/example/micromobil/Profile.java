package com.example.micromobil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Profile implements Serializable {
    private String name;
    private String type;
    private Map<String, List<Integer>> drinks;

    public Profile(String name, String type, List<Integer> temperatures) {
        this.name = name;
        this.type = type;
        this.drinks = new HashMap<>();
        this.drinks.put(type, temperatures); // Varsayılan içecek tipi ve sıcaklıkları ekle
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Integer> getTemperatures() {
        return drinks.getOrDefault(type, new ArrayList<>());
    }

    public void setTemperatures(List<Integer> temperatures) {
        this.drinks.put(type, temperatures);
    }

    public List<String> getDrinks() {
        return new ArrayList<>(drinks.keySet());
    }

    public List<Integer> getTemperaturesForDrink(String drink) {
        return drinks.getOrDefault(drink, new ArrayList<>());
    }
}
