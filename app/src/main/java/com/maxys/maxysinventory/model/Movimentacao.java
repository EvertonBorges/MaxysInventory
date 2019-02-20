package com.maxys.maxysinventory.model;

import java.util.Calendar;

public class Movimentacao implements Comparable<Movimentacao> {

    private Calendar dataHora;
    private boolean avariado;
    private int qtde;

    public Movimentacao() {
    }

    public Movimentacao(Calendar dataHora, boolean avariado, int qtde) {
        this.dataHora = dataHora;
        this.avariado = avariado;
        this.qtde = qtde;
    }

    public Calendar getDataHora() {
        return dataHora;
    }

    public void setDataHora(Calendar dataHora) {
        this.dataHora = dataHora;
    }

    public boolean isAvariado() {
        return avariado;
    }

    public void setAvariado(boolean avariado) {
        this.avariado = avariado;
    }

    public int getQtde() {
        return qtde;
    }

    public void setQtde(int qtde) {
        this.qtde = qtde;
    }

    @Override
    public int compareTo(Movimentacao o) {
        return this.getDataHora().compareTo(o.getDataHora());
    }
}
