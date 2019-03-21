package com.maxys.maxysinventory.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.Calendar;

public class Movimentacao implements Serializable, Comparable {

    private String idProduto;
    private Long dataHoraMovimentacao;
    private boolean avariado;
    private double qtde;
    private String idUsuario;
    private String nomeUsuario;
    private String emailUsuario;

    public Movimentacao() {
        this.idProduto = "";
        this.qtde = 0;
        this.idUsuario = "";
        this.nomeUsuario = "";
        this.emailUsuario = "";
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

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    @Exclude
    public Calendar getDataHora() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.dataHoraMovimentacao);
        return calendar;
    }

    @Override
    public int compareTo(Object o) {
        if (this.dataHoraMovimentacao.compareTo(((Movimentacao) o).getDataHoraMovimentacao()) > 0) {
            return -1;
        } else if (this.dataHoraMovimentacao.compareTo(((Movimentacao) o).getDataHoraMovimentacao()) < 0) {
            return 1;
        } else {
            return 0;
        }
    }
}