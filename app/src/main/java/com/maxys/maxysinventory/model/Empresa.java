package com.maxys.maxysinventory.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Empresa implements Serializable {

    private String id;
    private String nome;
    private Long dataHoraCriacao;

    public Empresa() {
        setDataHoraCriacao(Calendar.getInstance().getTimeInMillis());
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getDataHoraCriacao() {
        return dataHoraCriacao;
    }

    public void setDataHoraCriacao(Long dataHoraCriacao) {
        this.dataHoraCriacao = dataHoraCriacao;
    }

    @Exclude
    public Calendar getDataHora() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.dataHoraCriacao);
        return calendar;
    }

}