package com.maxys.maxysinventory.model;

import java.util.ArrayList;
import java.util.List;

public class Empresa {

    private String id;
    private String name;

    public Empresa() {
    }

    public Empresa(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}