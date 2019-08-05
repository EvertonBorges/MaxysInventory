package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.EmpresaAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoEmpresa;
import com.maxys.maxysinventory.model.TipoTransicaoEmpresa;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelecionaEmpresaActivity extends AppCompatActivity {

    private TipoTransicaoEmpresa tipoTransicao;
    private ArrayAdapter adapter;
    private List<Empresa> empresas;

    private PreferenciasStatic preferencias;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleciona_empresa);

        ListView lvEmpresas = findViewById(R.id.lv_empresa_selecionar);
        ImageButton ibAddEmpresa = findViewById(R.id.ib_empresa_adicionar);

        Intent intent = getIntent();
        tipoTransicao = (TipoTransicaoEmpresa) Objects.requireNonNull(intent.getExtras()).get("tipoTransicao");
        TipoSelecaoEmpresa tipoSelecao = (TipoSelecaoEmpresa) Objects.requireNonNull(intent.getExtras()).get("tipoSelecao");

        empresas = new ArrayList<>();

        adapter = new EmpresaAdapter(SelecionaEmpresaActivity.this, empresas);
        lvEmpresas.setAdapter(adapter);

        preferencias = PreferenciasStatic.getInstance();
        String idUsuario = preferencias.getIdUsuarioLogado();
        Usuario usuario = preferencias.getUsuario();

        List<String> permissoes = new ArrayList<>();
        for (Permissao permissao: usuario.getPermissoes()) {
            if (!permissoes.contains(permissao.getNome())) {
                permissoes.add(permissao.getNome());
            }
        }

        boolean permitirAdicionarEmpresa = permissoes.contains("actAdicionarEmpresa");

        ibAddEmpresa.setVisibility(permitirAdicionarEmpresa && tipoSelecao.equals(TipoSelecaoEmpresa.CRUD) ? View.VISIBLE : View.GONE);

        databaseReference = ConfiguracaoFirebase.getFirebase()
                                                .child("contribuidor_empresas")
                                                .child(idUsuario);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                empresas.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Empresa empresa = snapshot.getValue(Empresa.class);
                    empresas.add(empresa);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        lvEmpresas.setOnItemClickListener((parent, view, position, id) -> {
            Empresa empresa = empresas.get(position);

            abrirProximaTela(empresa);
        });

        ibAddEmpresa.setOnClickListener(v -> {
            Intent it = new Intent(SelecionaEmpresaActivity.this, EmpresaActivity.class);
            startActivity(it);
        });
    }

    private void abrirProximaTela(Empresa empresa) {
        switch (tipoTransicao) {
            case GERENCIAR_INVENTARIO:
                abrirProximaTela(empresa, InventarioActivity.class);
                break;
            case GERENCIAR_PRODUTO:
                abrirProximaTela(empresa, ProdutoMenuActivity.class);
                break;
            case GERENCIAR_EMPRESA:
                abrirProximaTela(empresa, EmpresaActivity.class);
                break;
            default :
                Util.AlertaInfo(SelecionaEmpresaActivity.this, "SELECIONE CONTRIBUIDOR", "Selecione uma empresa válida.");
                break;
        }
    }

    private void abrirProximaTela(Empresa empresa, Class classe) {
        DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
        reference.child("empresa_contribuidores")
                 .child(empresa.getId())
                 .child(Base64Custom.codificarBase64(preferencias.getUsuario().getEmail()))
                 .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if (dataSnapshot.getValue() != null) {
                             Contribuidor contribuidor = dataSnapshot.getValue(Contribuidor.class);
                             preferencias.salvarContribuidor(contribuidor);

                             Intent intent = new Intent(SelecionaEmpresaActivity.this, classe);
                             intent.putExtra("empresa", empresa);
                             startActivity(intent);
                         } else {
                             Util.AlertaInfo(SelecionaEmpresaActivity.this, "CONTRIBUIDOR", "Você não é contribuidor desta empresa.\n Feche o aplicativo e abra-o novamente.");
                         }
                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });
    }

    @Override
    protected void onStart() {
        super.onStart();

        databaseReference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        databaseReference.removeEventListener(valueEventListener);
    }
}