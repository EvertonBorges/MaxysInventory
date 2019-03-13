package com.maxys.maxysinventory.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Calendar;

public class Movimentacao implements Serializable {

    private String idProduto;
    private Long dataHoraMovimentacao;
    private boolean avariado;
    private double qtde;

    public Movimentacao() {
        dataHoraMovimentacao = Calendar.getInstance().getTimeInMillis();
    }

    public Long getDataHoraMovimentacao() {
        return dataHoraMovimentacao;
    }

    public void setDataHoraMovimentacao(Long dataHoraMovimentacao) {
        this.dataHoraMovimentacao = dataHoraMovimentacao;
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public boolean isAvariado() {
        return avariado;
    }

    public void setAvariado(boolean avariado) {
        this.avariado = avariado;
    }

    public double getQtde() {
        return qtde;
    }

    public void setQtde(double qtde) {
        this.qtde = qtde;
    }

    @Exclude
    public Calendar getDataHora() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.dataHoraMovimentacao);
        return calendar;
    }

}