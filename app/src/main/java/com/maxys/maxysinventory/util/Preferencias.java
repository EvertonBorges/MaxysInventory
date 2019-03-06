package com.maxys.maxysinventory.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class Preferencias {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private final String CHAVE_IDENTIFICADO = "identificadorUsuarioLogado";
    private final String CHAVE_NOME = "nomeUsuarioLogado";
    private final String CHAVE_ADMIN = "isAdmin";

    @SuppressLint("CommitPrefEdits")
    public Preferencias(Context contextoParamentro) {
        preferences = contextoParamentro.getSharedPreferences("maxysinventory.preferencias", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void salvarDados( String identificadorUsuario, String nomeUsuario, boolean isAdmin) {
        editor.putString(CHAVE_IDENTIFICADO, identificadorUsuario);
        editor.putString(CHAVE_NOME, nomeUsuario);
        editor.putBoolean(CHAVE_ADMIN, isAdmin);
        editor.commit();
    }

    public String getIdentificador() {
        return preferences.getString(CHAVE_IDENTIFICADO, null);
    }

    public String getNome() {
        return preferences.getString(CHAVE_NOME, null);
    }

    public Boolean isAdmin() {
        return preferences.getBoolean(CHAVE_ADMIN, false);
    }

}