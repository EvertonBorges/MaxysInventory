package com.maxys.maxysinventory.secondaryActivities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.config.Nomes;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.Util;

import java.util.Objects;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

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

    private final Handler handler = new Handler();
    private ProgressDialog progressDialog;

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
                } else if (s.toString().equals(edtConfirmSenha.getText().toString())) {
                    textLayoutConfirmSenha.setErrorEnabled(false);
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
            handler.post(() -> {
                if (progressDialog != null) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                progressDialog = Util.inicializaProgressDialog(CreateAccountActivity.this, "AGUARDE", "Realizando a criação da conta...");

                progressDialog.show();
            });

            FirebaseAuth firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

            Usuario usuario = new Usuario();
            usuario.setEmail(Objects.requireNonNull(edtLogin.getText()).toString());
            usuario.setNome(Objects.requireNonNull(edtNome.getText()).toString());

            firebaseAuth.createUserWithEmailAndPassword(usuario.getEmail(), Objects.requireNonNull(edtSenha.getText()).toString())
                        .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());

                    DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase()
                                                                              .child(Nomes.getChaveUsuario())
                                                                              .child(idUsuario);


                    databaseReference.setValue(usuario).addOnCompleteListener(command -> {
                        if (command.isSuccessful()) {
                            Util.salvarLog(null, idUsuario, "Criação de novo usuário.");

                            handler.post(() -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                            });

                            Util.Alerta(this, "SUCESSO", "Usuário criado com sucesso.").setOnDismissListener(dialog -> {
                                if (firebaseAuth.getCurrentUser() != null) {
                                    firebaseAuth.signOut();
                                }

                                finish();
                            }).show();
                        } else {
                            handler.post(() -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                            });

                            Util.AlertaInfo(this, "ERRO", "Tente novamente.");
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
        finish();

        super.onBackPressed();
    }

}
