package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
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
import com.maxys.maxysinventory.model.LogAcoes;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.Preferencias;
import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EmpresaActivity extends AppCompatActivity {

    private TextInputLayout tilEmpresa;
    private AppCompatEditText acetEmpresa;
    private EditText etContribuidor;
    private ImageButton ibAddContribuidor;
    private Button btSalvar;

    private ArrayAdapter adapter;
    private List<Contribuidor> contribuidores;
    private ListView lvContribuidores;

    private String idUsuarioLogado;

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
        ibAddContribuidor = findViewById(R.id.ib_empresa_add_contribuidor);
        btSalvar = findViewById(R.id.bt_empresa_salvar);

        Intent it = getIntent();
        empresa = (Empresa) it.getSerializableExtra("empresa");

        Preferencias preferencias = new Preferencias(EmpresaActivity.this);
        idUsuarioLogado = preferencias.getIdentificador();

        if (empresa == null) {
            empresa = new Empresa();
            empresa.setId(UUID.randomUUID().toString());
            empresa.setNome("");

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

            Contribuidor contribuidor = new Contribuidor();
            contribuidor.setEmail(Base64Custom.decodificarBase64(preferencias.getIdentificador()));
            contribuidor.setNome(preferencias.getNome());

            databaseReference.child("empresa_contribuidores")
                             .child(empresa.getId())
                             .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                             .setValue(contribuidor);

            databaseReference.child("contribuidor_empresas")
                             .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                             .child(empresa.getId())
                             .setValue(empresa);

            LogAcoes logAcoes = new LogAcoes();
            logAcoes.setIdUsuario(idUsuarioLogado);
            logAcoes.setIdEmpresa(empresa.getId());
            logAcoes.setDescricao("Criação de uma nova empresa.");
            logAcoes.salvarLog();
        } else {
            acetEmpresa.setText(empresa.getNome());
        }

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
                                if (command.isSuccessful()) {
                                    Toast.makeText(EmpresaActivity.this, "Contribuidor cadastrado com sucesso para a empresa!", Toast.LENGTH_LONG).show();

                                    LogAcoes logAcoes = new LogAcoes();
                                    logAcoes.setIdUsuario(idUsuarioLogado);
                                    logAcoes.setIdEmpresa(empresa.getId());
                                    logAcoes.setDescricao("Adicionado contribuidor (" + Base64Custom.codificarBase64(contribuidor.getEmail()) + ") para a empresa (" + empresa.getId() + ").");
                                    logAcoes.salvarLog();
                                } else {
                                    Util.AlertaInfo(EmpresaActivity.this, "ERRO CONTRIBUIDOR", "Por algum motivo falhou o cadastro do contribuidor para a empresa.");
                                }
                            });

                            reference.child("contribuidor_empresas")
                                    .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                    .child(empresa.getId())
                                    .setValue(empresa).addOnCompleteListener(command -> {
                                if (command.isSuccessful()) {
                                    Toast.makeText(EmpresaActivity.this, "Empresa cadastrada com sucesso para o contribuidor!", Toast.LENGTH_LONG).show();

                                    LogAcoes logAcoes = new LogAcoes();
                                    logAcoes.setIdUsuario(idUsuarioLogado);
                                    logAcoes.setIdEmpresa(empresa.getId());
                                    logAcoes.setDescricao("Adicionado empresa (" + empresa.getId() + ") para o contribuidor (" + Base64Custom.codificarBase64(contribuidor.getEmail()) + ").");
                                    logAcoes.salvarLog();
                                } else {
                                    Util.AlertaInfo(EmpresaActivity.this, "ERRO EMPRESA", "Por algum motivo falhou o cadastro da empresa para o contribuidor.");
                                }
                            });


                        } else {
                            Toast.makeText(EmpresaActivity.this, "Usuário não encontrado", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        btSalvar.setOnClickListener(v -> {
            String nomeEmpresa = acetEmpresa.getText().toString();
            if (nomeEmpresa.isEmpty()) {
                Util.AlertaInfo(EmpresaActivity.this, "NOME EMPRESA", "Nome da empresa deve ser informado!");
                acetEmpresa.requestFocus();
            } else {
                empresa.setNome(nomeEmpresa);
                DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
                reference.child("empresa").child(empresa.getId())
                        .setValue(empresa).addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
                        Toast.makeText(EmpresaActivity.this, "Empresa atualizada com sucesso!", Toast.LENGTH_LONG).show();

                        LogAcoes logAcoes = new LogAcoes();
                        logAcoes.setIdUsuario(idUsuarioLogado);
                        logAcoes.setIdEmpresa(empresa.getId());
                        logAcoes.setDescricao("Informações atualizadas para a empresa (" + empresa.getId() + ").");
                        logAcoes.salvarLog();

                        reference.child("contribuidor_empresas").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                    for (DataSnapshot snapshot1: snapshot.getChildren()) {
                                        if (empresa.getId().equals(snapshot1.getKey())) {
                                            reference.child("contribuidor_empresas")
                                                    .child(snapshot.getKey())
                                                    .child(snapshot1.getKey())
                                                    .setValue(empresa).addOnCompleteListener(command1 -> {
                                                if (command1.isSuccessful()) {
                                                    LogAcoes logAcoes = new LogAcoes();
                                                    logAcoes.setIdUsuario(idUsuarioLogado);
                                                    logAcoes.setIdEmpresa(empresa.getId());
                                                    logAcoes.setDescricao("Informações da empresa (" + snapshot1.getKey() + ") atualizadas para o contribuidor (" + snapshot.getKey() + ").");
                                                    logAcoes.salvarLog();
                                                }
                                            });
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    } else {
                        Toast.makeText(EmpresaActivity.this, "Erro ao atualizar a empresa.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
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