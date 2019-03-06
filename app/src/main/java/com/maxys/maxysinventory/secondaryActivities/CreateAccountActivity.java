package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.config.Nomes;
import com.maxys.maxysinventory.model.LogAcoes;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.Util;

import java.util.Calendar;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    private Button btnCriarConta;

    private AppCompatEditText edtNome;
    private AppCompatEditText edtLogin;
    private AppCompatEditText edtSenha;
    private AppCompatEditText edtConfirmSenha;

    private TextInputLayout textLayoutNome;
    private TextInputLayout textLayoutLogin;
    private TextInputLayout textLayoutSenha;
    private TextInputLayout textLayoutConfirmSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        btnCriarConta = findViewById(R.id.btnCriarConta);

        edtNome = findViewById(R.id.edtCreateNome);
        edtLogin = findViewById(R.id.edtCreateLogin);
        edtSenha = findViewById(R.id.edtCreateSenha);
        edtConfirmSenha = findViewById(R.id.edtCreateConfirmSenha);

        textLayoutNome = findViewById(R.id.textLayoutCreateNome);
        textLayoutLogin = findViewById(R.id.textLayoutCreateLogin);
        textLayoutSenha = findViewById(R.id.textLayoutCreateSenha);
        textLayoutConfirmSenha = findViewById(R.id.textLayoutCreateCornfimSenha);

        edtNome.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    textLayoutNome.setErrorEnabled(true);
                    textLayoutNome.setError("Preencha o nome.");
                } else {
                    textLayoutNome.setErrorEnabled(false);
                }
            }
        });

        edtLogin.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    textLayoutLogin.setErrorEnabled(true);
                    textLayoutLogin.setError("Preencha o e-mail.");
                } else {
                    textLayoutLogin.setErrorEnabled(false);
                }
            }
        });

        edtSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    textLayoutSenha.setErrorEnabled(true);
                    textLayoutSenha.setError("Preencha a senha.");
                } else if (s.toString().length() < 6) {
                    textLayoutSenha.setErrorEnabled(true);
                    textLayoutSenha.setError("Senha deve ter ao menos 6 caracteres.");
                } else {
                    textLayoutSenha.setErrorEnabled(false);
                }
            }
        });

        edtConfirmSenha.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) {
                    textLayoutConfirmSenha.setErrorEnabled(true);
                    textLayoutConfirmSenha.setError("Preencha a confirmação de senha.");
                } else if (!s.toString().equals(Objects.requireNonNull(edtSenha.getText()).toString())) {
                    textLayoutConfirmSenha.setErrorEnabled(true);
                    textLayoutConfirmSenha.setError("Senha difere da anterior.");
                } else {
                    textLayoutConfirmSenha.setErrorEnabled(false);
                }
            }
        });

        btnCriarConta.setOnClickListener(v -> CriarConta());
    }

    private void CriarConta() {
        if (textLayoutNome.isErrorEnabled() || textLayoutLogin.isErrorEnabled() || textLayoutSenha.isErrorEnabled() || textLayoutConfirmSenha.isErrorEnabled()) {
            if (textLayoutNome.isErrorEnabled()) {
                Util.AlertaInfo(this, "ERRO - NOME", "Verifique o nome");
                edtNome.requestFocus();
            } else if (textLayoutLogin.isErrorEnabled()) {
                Util.AlertaInfo(this, "ERRO - LOGIN", "Verifique o login");
                edtLogin.requestFocus();
            } else if (textLayoutSenha.isErrorEnabled()) {
                Util.AlertaInfo(this, "ERRO - SENHA", "Verifique a senha");
                edtSenha.requestFocus();
            } else if (textLayoutConfirmSenha.isErrorEnabled()) {
                Util.AlertaInfo(this, "ERRO - CONFIRMAÇÃO SENHA","Verifique a confirmação de senha");
                edtConfirmSenha.requestFocus();
            }
        } else {
            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

            Usuario usuario = new Usuario();
            usuario.setEmail(edtLogin.getText().toString());
            usuario.setNome(Objects.requireNonNull(edtNome.getText()).toString());
            usuario.setAdmin(false);

            firebaseAuth.createUserWithEmailAndPassword(usuario.getEmail(), Objects.requireNonNull(edtSenha.getText()).toString())
                        .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());

                    DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase()
                                                                              .child(Nomes.getChaveUsuario())
                                                                              .child(idUsuario);


                    databaseReference.setValue(usuario).addOnCompleteListener(command -> {
                        if (command.isSuccessful()) {
                            String acao = "Criação de novo usuário.";

                            LogAcoes logAcoes = new LogAcoes();
                            logAcoes.setIdUsuario(idUsuario);
                            logAcoes.setDescricao(acao);
                            logAcoes.salvarLog();

                            Util.AlertaInfo(this, "SUCESSO", "Usuário criado com SUCESSO.").setOnDismissListener(dialog -> {
                                if (firebaseAuth.getCurrentUser() != null) {
                                    firebaseAuth.signOut();
                                }

                                finish();
                            });
                        }
                    });
                } else {
                    if (task.getException().getClass().equals(FirebaseAuthUserCollisionException.class)) {
                        Util.AlertaInfo(this, "ERRO", "O e-mail de endereço já está sendo usado. Favor entrar em contato com a equipe de suporte!!!");
                    } else if (task.getException().getClass().equals(FirebaseNetworkException.class)) {
                        Util.AlertaInfo(this, "ERRO", "Dispositivo precisa estar conectado a internet para efetuar esta ação.");
                    } else {
                        Util.AlertaInfo(this, "ERRO", "Houve um erro ao tentar criar o usuário. \n" +
                                                                            "Erro (" + Objects.requireNonNull(task.getException()).getClass() + "): " +
                                                                            Objects.requireNonNull(task.getException()).getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();

        super.onBackPressed();
    }

}
