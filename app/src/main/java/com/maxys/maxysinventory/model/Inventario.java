package com.maxys.maxysinventory.model;

import android.os.Build;

import com.google.firebase.database.Exclude;

import java.util.Calendar;
import java.util.Objects;

import androidx.annotation.RequiresApi;

public class Inventario {

    private String codReferencia;
    private String descricao;
    private double saldo;
    private double avariados;
    private Long dataHoraMovimentacao;

    public Inventario() {
        this.saldo = 0;
        this.avariados = 0;
        this.dataHoraMovimentacao = Calendar.getInstance().getTimeInMillis();
    }

    public String getCodReferencia() {
        return codReferencia;
    }

    public void setCodReferencia(String codReferencia) {
        this.codReferencia = codReferencia;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getDataHoraMovimentacao() {
        return dataHoraMovimentacao;
    }

    public void setDataHoraMovimentacao(Long dataHoraMovimentacao) {
        this.dataHoraMovimentacao = dataHoraMovimentacao;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public double getAvariados() {
        return avariados;
    }

    public void setAvariados(double avariados) {
        this.avariados = avariados;
    }

    @Exclude
    public void addSaldo(double qtde) {
        this.saldo += qtde;
    }

    @Exclude
    public void addAvarias(double qtde) {
        this.avariados += qtde;
    }

    @Exclude
    public Calendar getDataHora() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.dataHoraMovimentacao);
        return calendar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventario that = (Inventario) o;
        return that.getCodReferencia().equals(codReferencia);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(codReferencia);
    }
}