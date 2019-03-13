package com.maxys.maxysinventory.util;

import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Usuario;

public class PreferenciasStatic {

    private static PreferenciasStatic instance;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Contribuidor contribuidor;

    private PreferenciasStatic() {

    }

    public static synchronized PreferenciasStatic getInstance() {
        if (instance == null) {
            instance = new PreferenciasStatic();
        }

        return instance;
    }

    public void salvarUsuario(Usuario usuario, String idUsuarioLogado) {
        this.usuario = usuario;
        this.idUsuarioLogado = idUsuarioLogado;
    }

    public void salvarContribuidor(Contribuidor contribuidor) {
        this.contribuidor = contribuidor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getIdUsuarioLogado() {
        return idUsuarioLogado;
    }

    public Contribuidor getContribuidor() {
        return contribuidor;
    }

    public void removerContribuidor() {
        this.contribuidor = null;
    }

    public void removerUsuario() {
        this.usuario = null;
        this.idUsuarioLogado = null;
    }

}