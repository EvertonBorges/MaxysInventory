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
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoEmpresa;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.model.TipoTransicaoEmpresa;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.secondaryActivities.LoginActivity;
import com.maxys.maxysinventory.secondaryActivities.PermissaoActivity;
import com.maxys.maxysinventory.secondaryActivities.SelecionaEmpresaActivity;
import com.maxys.maxysinventory.secondaryActivities.UsuarioActivity;
import com.maxys.maxysinventory.util.PreferenciasStatic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class PrincipalActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private PreferenciasStatic preferencias;

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatadorData = new SimpleDateFormat("dd/MM/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        TextView txtUsuario = findViewById(R.id.txt_principal_usuario);
        TextView txtData = findViewById(R.id.txt_principal_data);
        Button btnPermissoes = findViewById(R.id.btn_principal_permissoes);
        Button btnEmpresa = findViewById(R.id.btn_principal_empresa);
        Button btnProduto = findViewById(R.id.btn_principal_produtos);
        Button btnInventario = findViewById(R.id.btn_principal_inventario);
        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle("Maxys Inventory");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);

        preferencias = PreferenciasStatic.getInstance();
        Usuario usuario = preferencias.getUsuario();
        txtUsuario.setText(usuario.getNome());

        List<String> permissoes = new ArrayList<>();
        for (Permissao permissao: usuario.getPermissoes()) {
            if (!permissoes.contains(permissao.getNome())) {
                permissoes.add(permissao.getNome());
            }
        }

        boolean permitirVisualizarEmpresa = permissoes.contains("actMenuEmpresa");
        boolean permitirVisualizarPermissoesGerais = permissoes.contains("actPermissoesGerais");

        btnEmpresa.setVisibility(permitirVisualizarEmpresa ? View.VISIBLE : View.GONE);
        btnPermissoes.setVisibility(permitirVisualizarPermissoesGerais ? View.VISIBLE : View.GONE);

        String data = "Data: " + formatadorData.format(Calendar.getInstance().getTime());
        txtData.setText(data);

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

        btnPermissoes.setOnClickListener(v -> {
            Intent intent = new Intent(PrincipalActivity.this, UsuarioActivity.class);

            startActivity(intent);
        });

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
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }

        preferencias.removerUsuario();

        Intent intent = new Intent(PrincipalActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

}
