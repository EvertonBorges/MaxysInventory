package com.maxys.maxysinventory;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.os.Bundle;
import android.widget.ListView;

import com.maxys.maxysinventory.adapter.PermissaoUsuarioAdapter;

import java.util.HashMap;

public class PermissaoActivity extends AppCompatActivity {

    private AppCompatTextView tvTitulo;

    private PermissaoUsuarioAdapter adapter;
    private ListView listViewPermissoes;

    private final HashMap<String, String> permissoes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissao);


    }
}
