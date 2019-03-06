package com.maxys.maxysinventory;

import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.TipoSelecaoEmpresa;
import com.maxys.maxysinventory.model.TipoTransicaoEmpresa;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.secondaryActivities.LoginActivity;
import com.maxys.maxysinventory.secondaryActivities.SelecionaEmpresaActivity;
import com.maxys.maxysinventory.util.Preferencias;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PrincipalActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatadorData = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        TextView txtUsuario = findViewById(R.id.txt_principal_usuario);
        TextView txtData = findViewById(R.id.txt_principal_data);
        Button btnEmpresa = findViewById(R.id.btn_principal_empresa);
        Button btnProduto = findViewById(R.id.btn_principal_produtos);
        Button btnInventario = findViewById(R.id.btn_principal_inventario);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("Maxys Inventory");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);

        Preferencias preferencias = new Preferencias(this);
        txtUsuario.setText(preferencias.getNome());

        btnEmpresa.setVisibility(preferencias.isAdmin() ? View.VISIBLE : View.GONE);

        String data = "Data: " + formatadorData.format(Calendar.getInstance().getTime());
        txtData.setText(data);

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

        btnEmpresa.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, SelecionaEmpresaActivity.class);
            intent.putExtra("tipoTransicao", TipoTransicaoEmpresa.GERENCIAR_EMPRESA);
            intent.putExtra("tipoSelecao", TipoSelecaoEmpresa.CRUD);

            startActivity(intent);
        });

        btnProduto.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, SelecionaEmpresaActivity.class);
            intent.putExtra("tipoTransicao", TipoTransicaoEmpresa.GERENCIAR_PRODUTO);
            intent.putExtra("tipoSelecao", TipoSelecaoEmpresa.SELECAO);

            startActivity(intent);
        });

        btnInventario.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, SelecionaEmpresaActivity.class);
            intent.putExtra("tipoTransicao", TipoTransicaoEmpresa.GERENCIAR_INVENTARIO);
            intent.putExtra("tipoSelecao", TipoSelecaoEmpresa.SELECAO);

            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_sair:
                deslogarUsuario();
                return true;
            case R.id.item_configuracoes:
                return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    private void deslogarUsuario() {
        firebaseAuth.signOut();
        Intent intent = new Intent(PrincipalActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
