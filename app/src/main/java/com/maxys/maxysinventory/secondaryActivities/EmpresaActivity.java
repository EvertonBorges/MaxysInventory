package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.ContribuidorAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EmpresaActivity extends AppCompatActivity {

    private TextInputLayout tilEmpresa;
    private AppCompatEditText acetEmpresa;
    private EditText etContribuidor;
    private ImageButton ibPermissoes;
    private ImageButton ibAddContribuidor;
    private Button btSalvar;

    private ArrayAdapter adapter;
    private List<Contribuidor> contribuidores;
    private ListView lvContribuidores;

    // Informações da sessão.
    private String idUsuarioLogado;
    private Usuario usuario;
    private Contribuidor contribuidor;
    private Empresa empresa;

    private DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empresa);

        tilEmpresa = findViewById(R.id.ti_empresa_nome);
        acetEmpresa = findViewById(R.id.et_empresa_nome);
        etContribuidor = findViewById(R.id.et_empresa_novo_contribuidor);
        ibPermissoes = findViewById(R.id.ib_permissoes);
        ibAddContribuidor = findViewById(R.id.ib_empresa_add_contribuidor);
        btSalvar = findViewById(R.id.bt_empresa_salvar);

        Intent it = getIntent();
        empresa = (Empresa) it.getSerializableExtra("empresa");

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        usuario = preferencias.getUsuario();
        idUsuarioLogado = preferencias.getIdUsuarioLogado();

        if (empresa == null) {
            empresa = new Empresa();
            empresa.setId(UUID.randomUUID().toString());
            empresa.setNome("");
            empresa.setDataHoraCriacao(Calendar.getInstance().getTimeInMillis());

            DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase();
            databaseReference.child("empresa").child(empresa.getId())
                             .setValue(empresa).addOnCompleteListener(command -> {
                if (command.isSuccessful()) {
                    Toast.makeText(EmpresaActivity.this, "Empresa criada com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(EmpresaActivity.this, "Erro ao criar a Empresa.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

            contribuidor = new Contribuidor();
            contribuidor.setEmail(usuario.getEmail());
            contribuidor.setNome(usuario.getNome());

            databaseReference.child("permissoes_contribuidores")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            List<Permissao> permissoes = new ArrayList<>();

                            if (dataSnapshot.getValue() != null) {
                                for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    Permissao permissao = snapshot.getValue(Permissao.class);

                                    if (!permissoes.contains(permissao)) {
                                        permissoes.add(permissao);
                                    }
                                }
                            }

                            databaseReference.child("empresa_contribuidores")
                                    .child(empresa.getId())
                                    .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                    .setValue(contribuidor);

                            databaseReference.child("contribuidor_empresas")
                                    .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                    .child(empresa.getId())
                                    .setValue(empresa);

                            Util.salvarLog(empresa.getId(), idUsuarioLogado, "COMPANY - CREATED");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        } else {
            acetEmpresa.setText(empresa.getNome());
            contribuidor = preferencias.getContribuidor();
        }

        List<String> permissoes = new ArrayList<>();
        for (Permissao permissao: contribuidor.getPermissoes()) {
            if (!permissoes.contains(permissao.getNome())) {
                permissoes.add(permissao.getNome());
            }
        }

        boolean permitirEditarPermissoesContribuidores = permissoes.contains("actPermissoesContribuidorEmpresa");
        boolean permitirEditarPermissoes = permissoes.contains("actPermissoesEmpresa");

        ibPermissoes.setVisibility(permitirEditarPermissoes ? View.VISIBLE : View.GONE);

        contribuidores = new ArrayList<>();

        lvContribuidores = findViewById(R.id.lv_empresa_contribuidores);
        adapter = new ContribuidorAdapter(EmpresaActivity.this, contribuidores, empresa.getId());
        lvContribuidores.setAdapter(adapter);

        databaseReference = ConfiguracaoFirebase.getFirebase()
                                                .child("empresa_contribuidores")
                                                .child(empresa.getId());

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contribuidores.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Contribuidor contribuidor = snapshot.getValue(Contribuidor.class);
                    contribuidores.add(contribuidor);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        ibAddContribuidor.setOnClickListener(v -> {
            String emailContribuidor = etContribuidor.getText().toString();
            if (emailContribuidor.isEmpty()) {
                Util.AlertaInfo(EmpresaActivity.this, "E-MAIL CONTRIBUIDOR", "Informe o e-mail do contribuidor!");
                etContribuidor.requestFocus();
            } else {
                Contribuidor contribuidor = new Contribuidor();
                contribuidor.setEmail(emailContribuidor);

                DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
                reference.child("usuarios")
                         .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                         .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            Usuario usuario = dataSnapshot.getValue(Usuario.class);

                            contribuidor.setNome(Objects.requireNonNull(usuario).getNome());

                            reference.child("empresa_contribuidores")
                                     .child(empresa.getId())
                                     .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                     .setValue(contribuidor).addOnCompleteListener(command -> {
                                if (!command.isSuccessful()) {
                                    Util.salvarLog(empresa.getId(), idUsuarioLogado, "Erro ao adicionar contribuidor '" + Base64Custom.codificarBase64(contribuidor.getEmail()) + "' a empresa.");
                                }
                            });

                            reference.child("contribuidor_empresas")
                                    .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                    .child(empresa.getId())
                                    .setValue(empresa).addOnCompleteListener(command -> {
                                if (!command.isSuccessful()) {
                                    Util.salvarLog(empresa.getId(), idUsuarioLogado, "Erro ao adicionar empresa ao contribuidor '" + Base64Custom.codificarBase64(contribuidor.getEmail()) + "' .");
                                }
                            });
                        } else {
                            Util.AlertaInfo(EmpresaActivity.this, "ERRO USUÁRIO", "Usuário não encontrado");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        btSalvar.setOnClickListener(v -> {
            String nomeEmpresa = Objects.requireNonNull(acetEmpresa.getText()).toString();
            if (nomeEmpresa.isEmpty()) {
                Util.AlertaInfo(EmpresaActivity.this, "NOME CONTRIBUIDOR", "Nome da empresa deve ser informado!");
                acetEmpresa.requestFocus();
            } else {
                empresa.setNome(nomeEmpresa);
                DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
                reference.child("empresa")
                         .child(empresa.getId())
                         .setValue(empresa)
                         .addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {

                        Util.salvarLog(empresa.getId(), idUsuarioLogado, "COMPANY - UPDATED");

                        reference.child("contribuidor_empresas")
                                 .orderByChild(empresa.getId() + "/id")
                                 .equalTo(empresa.getId())
                                 .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        snapshot.getRef()
                                                .child(empresa.getId())
                                                .setValue(empresa)
                                                .addOnCompleteListener(command1 -> {
                                            if (!command1.isSuccessful()) {
                                                Util.salvarLog(empresa.getId(), idUsuarioLogado, "Erro ao atualizar informações da empresa, para o contribuidor '" + snapshot.getKey() + "'.");
                                            }
                                        });
                                    }
                                    Util.AlertaInfo(EmpresaActivity.this, "CONTRIBUIDOR ATUALIZADA", "Empresa atualizada com sucesso.", (dialog, which) -> {
                                        finish();
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Util.AlertaInfo(EmpresaActivity.this, "ERRO", "Erro ao atualizar a empresa.");
                    }
                });
            }
        });

        lvContribuidores.setOnItemClickListener((parent, view, position, id) -> {
            Contribuidor contribuidor = contribuidores.get(position);

            Intent intent = new Intent(EmpresaActivity.this, PermissaoActivity.class);
            intent.putExtra("idContribuidorSelecionado", Base64Custom.codificarBase64(contribuidor.getEmail()));
            intent.putExtra("tipoSelecaoPermissao", TipoSelecaoPermissao.CONTRIBUIDOR.toString());
            intent.putExtra("contribuidor", contribuidor);
            intent.putExtra("empresa", empresa);
            startActivity(intent);
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