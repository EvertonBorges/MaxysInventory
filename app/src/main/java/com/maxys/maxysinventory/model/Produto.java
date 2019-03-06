package com.maxys.maxysinventory.model;

import android.os.Build;

import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Produto implements Comparable<Produto> {

    private String descricao;
    private String codReferencia;
    private String usuarioCadastro;
    private HashMap<String, Movimentacao> movimentacoes;

    public Produto() {
        this.movimentacoes = new HashMap<>();
    }

    public Produto(int id, String descricao, String codReferencia, String usuarioCadastro) {
        this.descricao = descricao;
        this.codReferencia = codReferencia;
        this.usuarioCadastro = usuarioCadastro;
        this.movimentacoes = new HashMap<>();
    }

    public Produto(String descricao, String codReferencia, String usuarioCadastro, HashMap<String, Movimentacao> movimentacoes) {
        this.descricao = descricao;
        this.codReferencia = codReferencia;
        this.usuarioCadastro = usuarioCadastro;
        this.movimentacoes = movimentacoes;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCodReferencia() {
        return codReferencia;
    }

    public void setCodReferencia(String codReferencia) {
        this.codReferencia = codReferencia;
    }

    public String getUsuarioCadastro() {
        return usuarioCadastro;
    }

    public void setUsuarioCadastro(String usuarioCadastro) {
        this.usuarioCadastro = usuarioCadastro;
    }

    public HashMap<String, Movimentacao> getMovimentacoes() {
        return movimentacoes;
    }

    public void setMovimentacoes(HashMap<String, Movimentacao> movimentacoes) {
        this.movimentacoes = movimentacoes;
    }

    public int getSaldo() {
        int saldo = 0;

        for (Movimentacao movimentacao: movimentacoes.values()) {
            if (!movimentacao.isAvariado()) {
                saldo += movimentacao.getQtde();
            }
        }

        return saldo;
    }

    public int getAvariados() {
        int avariados = 0;

        for (Movimentacao movimentacao: movimentacoes.values()) {
            if (movimentacao.isAvariado()) {
                avariados += movimentacao.getQtde();
            }
        }

        return avariados;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(produto.getCodReferencia(), this.codReferencia) &&
                   Objects.equals(produto.getDescricao(), this.descricao);
        } else {
            return produto.getDescricao().equals(this.descricao) && produto.getCodReferencia().equals(this.codReferencia);
        }
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.hash(descricao, codReferencia);
        } else {
            return this.hashCode();
        }
    }

    @Override
    public String toString() {
        int saldo = 0;
        int avariados = 0;

        for (Movimentacao movimentacao: movimentacoes.values()) {
            if (movimentacao.isAvariado()) {
                avariados += movimentacao.getQtde();
            } else {
                saldo += movimentacao.getQtde();
            }
        }

        String saldoTexto = (saldo / 10) < 1 ? "  " + saldo : (saldo / 100) < 1 ? " " + saldo : String.valueOf(saldo);
        String avariadosTexto = (avariados / 10) < 1 ? "  " + avariados : (avariados / 100) < 1 ? " " + avariados : String.valueOf(avariados);

        return codReferencia + " - " + descricao + "\n(Saldo = " + saldoTexto + "; Avariados = " + avariadosTexto + " )";
    }

    @Override
    public int compareTo(Produto o) {
        Calendar ultimaData = Util.maiorCalendar(this.movimentacoes.values());
        Calendar dataCompare = Util.maiorCalendar(o.getMovimentacoes().values());

        if (dataCompare == null && ultimaData == null) {
            return 0;
        } else {
            if (dataCompare == null) {
                return -1;
            } else if (ultimaData == null) {
                return 1;
            } else {
                return dataCompare.compareTo(ultimaData);
            }
        }
    }
}