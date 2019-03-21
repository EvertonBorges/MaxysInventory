package com.maxys.maxysinventory.secondaryActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.PermissaoAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PermissaoActivity extends AppCompatActivity {

    private PermissaoAdapter adapter;
    private ListView listViewPermissoes;

    private List<Permissao> permissoes = new ArrayList<>();

    private DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
    private ValueEventListener valueEventListener;

    private boolean isPermissaoGeral;
    private TipoSelecaoPermissao tipoSelecaoPermissao;
    private String idUsuarioSelecionado;
    private String idContribuidorSelecionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissao);

        Intent intent = getIntent();
        idUsuarioSelecionado = Objects.requireNonNull(intent.getExtras()).getString("idUsuarioSelecionado");
        Usuario usuario = (Usuario) intent.getExtras().get("usuario");
        idContribuidorSelecionado = Objects.requireNonNull(intent.getExtras()).getString("idContribuidorSelecionado");
        Contribuidor contribuidor = (Contribuidor) intent.getExtras().get("contribuidor");
        String idEmpresa = intent.getExtras().getString("idEmpresa");
        tipoSelecaoPermissao = TipoSelecaoPermissao.valueOf(intent.getExtras().getString("tipoSelecaoPermissao", "GERAL"));
        isPermissaoGeral = tipoSelecaoPermissao.equals(TipoSelecaoPermissao.GERAL);

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        String idUsuarioLogado = preferencias.getIdUsuarioLogado();

        AppCompatTextView tvTitulo = findViewById(R.id.tv_permissao_titulo);
        AppCompatButton btnSalvar = findViewById(R.id.bt_permissao_salvar);
        listViewPermissoes = findViewById(R.id.lv_permissoes);

        tvTitulo.setText(isPermissaoGeral ? "Permissões de usuário.": "Permissões de contribuidor");
        btnSalvar.setOnClickListener(v -> {
            if (isPermissaoGeral) {
                reference.child("usuarios")
                         .child(idUsuarioSelecionado)
                         .addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 if (dataSnapshot.getValue() != null) {
                                     Usuario usuario = dataSnapshot.getValue(Usuario.class);

                                     AlertDialog.Builder builder = Util.Alerta(PermissaoActivity.this, "PERMISSÕES", "Deseja realmente salvar as permissões?").setCancelable(true);
                                     builder.setPositiveButton("Sim", (dialog, which) -> {
                                         List<Permissao> permissoesDadas = new ArrayList<>();
                                         List<Permissao> permissoesRemovidas = new ArrayList<>();

                                         for (int i = 0; i < listViewPermissoes.getChildCount(); i++) {
                                             if (((CheckBox)listViewPermissoes.getChildAt(i).findViewById(R.id.chk_permissao_ativo)).isChecked()) {
                                                 if (!usuario.getPermissoes().contains(permissoes.get(i))) {
                                                     usuario.getPermissoes().add(permissoes.get(i));
                                                     permissoesDadas.add(permissoes.get(i));
                                                 }
                                             } else {
                                                 if (usuario.getPermissoes().contains(permissoes.get(i))) {
                                                     usuario.getPermissoes().remove(permissoes.get(i));
                                                     permissoesRemovidas.add(permissoes.get(i));
                                                 }
                                             }
                                         }

                                         reference.child("usuarios")
                                                  .child(idUsuarioSelecionado)
                                                  .setValue(usuario)
                                                  .addOnCompleteListener(task -> {
                                                      if (task.isSuccessful()) {
                                                          Util.salvarLog(null, idUsuarioLogado, "Atualização de permissões para o usuário '" + idUsuarioSelecionado + "':" + (permissoesDadas.isEmpty() ? "" : "\nNovas=" + permissoesDadas.toString()) + (permissoesRemovidas.isEmpty() ? "": "\nRemovidas=" + permissoesRemovidas.toString()));

                                                          Util.AlertaInfo(PermissaoActivity.this, "PERMISSÕES", "Permissões atualizadas com sucesso.", (dialog1, which1) -> finish());
                                                      } else {
                                                          Util.salvarLog(null, idUsuarioLogado, "Falha ao tentar atualizar permissões do usuário '" + idUsuarioSelecionado + "'");

                                                          Util.AlertaInfo(PermissaoActivity.this, "PERMISSÕES", "Falha ao tentar atualizar as permissões.\nTente novamente.");
                                                      }
                                                  });
                                     }).setNegativeButton("Não", null).show();
                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         });
            } else {
                reference.child("empresa_contribuidores")
                        .child(idEmpresa)
                        .child(idContribuidorSelecionado)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    Contribuidor contribuidor = dataSnapshot.getValue(Contribuidor.class);

                                    AlertDialog.Builder builder = Util.Alerta(PermissaoActivity.this, "PERMISSÕES", "Deseja realmente salvar as permissões?").setCancelable(true);
                                    builder.setPositiveButton("Sim", (dialog, which) -> {
                                        List<Permissao> permissoesDadas = new ArrayList<>();
                                        List<Permissao> permissoesRemovidas = new ArrayList<>();

                                        for (int i = 0; i < listViewPermissoes.getChildCount(); i++) {
                                            if (((CheckBox)listViewPermissoes.getChildAt(i).findViewById(R.id.chk_permissao_ativo)).isChecked()) {
                                                if (!contribuidor.getPermissoes().contains(permissoes.get(i))) {
                                                    contribuidor.getPermissoes().add(permissoes.get(i));
                                                    permissoesDadas.add(permissoes.get(i));
                                                }
                                            } else {
                                                if (contribuidor.getPermissoes().contains(permissoes.get(i))) {
                                                    contribuidor.getPermissoes().remove(permissoes.get(i));
                                                    permissoesRemovidas.add(permissoes.get(i));
                                                }
                                            }
                                        }

                                        reference.child("empresa_contribuidores")
                                                .child(idEmpresa)
                                                .child(idContribuidorSelecionado)
                                                .setValue(contribuidor)
                                                .addOnCompleteListener(task -> {
                                                    if (task.isSuccessful()) {
                                                        Util.salvarLog(null, idUsuarioLogado, "Atualização de permissões para o contribuidor '" + idContribuidorSelecionado + "':" + (permissoesDadas.isEmpty() ? "" : "\nNovas=" + permissoesDadas.toString()) + (permissoesRemovidas.isEmpty() ? "": "\nRemovidas=" + permissoesRemovidas.toString()));

                                                        Util.AlertaInfo(PermissaoActivity.this, "PERMISSÕES", "Permissões atualizadas com sucesso.", (dialog1, which1) -> finish());
                                                    } else {
                                                        Util.salvarLog(null, idUsuarioLogado, "Falha ao tentar atualizar permissões do contribuidor '" + idContribuidorSelecionado + "'");

                                                        Util.AlertaInfo(PermissaoActivity.this, "PERMISSÕES", "Falha ao tentar atualizar as permissões.\nTente novamente.");
                                                    }
                                                });
                                    }).setNegativeButton("Não", null).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
            }


        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                permissoes.clear();

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        Permissao permissao = snapshot.getValue(Permissao.class);

                        permissoes.add(permissao);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        adapter = new PermissaoAdapter(PermissaoActivity.this, permissoes, tipoSelecaoPermissao, usuario, contribuidor);
        listViewPermissoes.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (tipoSelecaoPermissao.equals(TipoSelecaoPermissao.GERAL)) {
            reference.child("permissoes_gerais").addValueEventListener(valueEventListener);
        } else {
            reference.child("permissoes_contribuidores").addValueEventListener(valueEventListener);
        }
    }
}