package com.maxys.maxysinventory.secondaryActivities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.TipoSelecaoEmpresa;
import com.maxys.maxysinventory.model.TipoTransicaoEmpresa;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Preferencias;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ProdutoMenuActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatadorData = new SimpleDateFormat("dd/MM/yyyy");

    private Empresa empresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_menu);

        TextView edtNomeUsuario = findViewById(R.id.txt_menu_produto_usuario);
        TextView edtDataHora = findViewById(R.id.txt_menu_produto_data);
        Button btnImportar = findViewById(R.id.btn_menu_produto_importar);
        Button btnExportar = findViewById(R.id.btn_menu_produto_exportar);
        Button btnGerenciar = findViewById(R.id.btn_menu_produto_gerenciar);

        Preferencias preferencias = new Preferencias(ProdutoMenuActivity.this);
        String nome = preferencias.getNome();
        edtNomeUsuario.setText(nome);

        Intent it = getIntent();
        empresa = (Empresa) it.getSerializableExtra("empresa");

        String data = "Data: " + formatadorData.format(Calendar.getInstance().getTime());
        edtDataHora.setText(data);

        btnImportar.setOnClickListener(v -> {
            //Intent it = new Intent(ProdutoMenuActivity.this, );
            //it.putExtra("contribuidor", contribuidor);

            //startActivity(it);
        });

        btnExportar.setOnClickListener(v -> {
            //Intent it = new Intent(ProdutoMenuActivity.this, );
            //it.putExtra("contribuidor", contribuidor);

            //startActivity(it);
        });

        btnGerenciar.setOnClickListener(v -> {
            Intent intent = new Intent(ProdutoMenuActivity.this, ManageProdutoActivity.class);
            intent.putExtra("empresa", empresa);
            startActivity(intent);
        });

    }

}