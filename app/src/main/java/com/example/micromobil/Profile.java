package com.example.micromobil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Profile implements Serializable {
    private String name;
    private Map<String, List<Integer>> drinks;

    public Profile(String name, List<Integer> temperatures) {
        this.name = name;
        this.drinks = new HashMap<>();
        this.drinks.put(name, temperatures);
    }

    public Profile(String name, Map<String, List<Integer>> drinks) {
        this.name = name;
        this.drinks = drinks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getTemperatures() {
        return drinks.getOrDefault(name, new ArrayList<>());
    }

    public void setTemperatures(List<Integer> temperatures) {
        this.drinks.put(name, temperatures);
    }

    public List<String> getDrinks() {
        return new ArrayList<>(drinks.keySet());
    }

    public List<Integer> getTemperaturesForDrink(String drink) {
        return drinks.getOrDefault(drink, new ArrayList<>());
    }

    public void setDrinks(Map<String, List<Integer>> drinkTemperatures) {
        this.drinks = drinkTemperatures;
    }

    public Map<String, List<Integer>> getDrinkTemperatures() {
        return drinks;
    }

    public void setDrinkTemperatures(Map<String, List<Integer>> drinkTemperatures) {
        this.drinks = drinkTemperatures;
    }
}
