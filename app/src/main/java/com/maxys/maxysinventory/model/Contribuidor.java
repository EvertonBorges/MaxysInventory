package com.maxys.maxysinventory.model;

import java.io.Serializable;

public class Contribuidor implements Serializable {

    private String nome;
    private String email;

    public Contribuidor() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}