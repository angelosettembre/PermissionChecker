package com.example.angiopasqui.permissionchecker;

/**
 * Created by Angiopasqui on 04/10/2017.
 */

public class Permesso {

    private String name;
    private String description;

    public Permesso(){}

    public Permesso(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
