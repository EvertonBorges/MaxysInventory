package com.maxys.maxysinventory.model;

public class Contribuidor {

    private String id;
    private String nome;

    public Contribuidor() {
    }

    public Contribuidor(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}