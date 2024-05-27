package com.example.micromobil;

import java.io.Serializable;
import java.util.List;

public class Profile implements Serializable {
    private String name;
    private String type;
    private List<Integer> temperatures;

    public Profile(String name, String type, List<Integer> temperatures) {
        this.name = name;
        this.type = type;
        this.temperatures = temperatures;
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
        return temperatures;
    }

    public void setTemperatures(List<Integer> temperatures) {
        this.temperatures = temperatures;
    }
}
