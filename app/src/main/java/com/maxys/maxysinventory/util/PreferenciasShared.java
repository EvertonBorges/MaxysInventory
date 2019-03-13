package com.maxys.maxysinventory.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class PreferenciasShared {

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private final String CHAVE_LOGIN = "loginUltimoUso";
    private final String CHAVE_SENHA = "senhaUltimoUso";

    @SuppressLint("CommitPrefEdits")
    public PreferenciasShared(Context contextoParamentro) {
        preferences = contextoParamentro.getSharedPreferences("maxysinventory.preferencias", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void salvarDados( String login, String senha) {
        editor.putString(CHAVE_LOGIN, Base64Custom.codificarBase64(login));
        editor.putString(CHAVE_SENHA, Base64Custom.codificarBase64(senha));
        editor.commit();
    }

    public String getLogin() {
        return preferences.getString(CHAVE_LOGIN, "");
    }

    public String getSenha() {
        return preferences.getString(CHAVE_SENHA, "");
    }
}