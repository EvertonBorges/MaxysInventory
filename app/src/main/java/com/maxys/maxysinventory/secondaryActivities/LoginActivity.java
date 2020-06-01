package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.PrincipalActivity;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.config.Nomes;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.PreferenciasShared;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private AppCompatEditText edtLogin;
    private AppCompatEditText edtSenha;
    private AppCompatButton btnEntrar;
    private AppCompatButton btnCadastrar;

    private TextInputLayout textLayoutLogin;
    private TextInputLayout textLayoutSenha;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        solicitarPermissoes();

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();

        Handler handler = new Handler();

        btnEntrar = findViewById(R.id.bt_login_logar);
        btnCadastrar = findViewById(R.id.bt_login_cadastrar);

        edtLogin = findViewById(R.id.edtLogin);
        edtSenha = findViewById(R.id.edtSenha);

        textLayoutLogin = findViewById(R.id.textLayoutCreateLogin);
        textLayoutSenha = findViewById(R.id.textLayoutCreateSenha);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        PreferenciasShared preferencias = new PreferenciasShared(LoginActivity.this);
        edtLogin.setText(Base64Custom.decodificarBase64(preferencias.getLogin()));
        edtSenha.setText(Base64Custom.decodificarBase64(preferencias.getSenha()));

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
                    textLayoutLogin.setError("Preencha o login.");
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
                } else {
                    textLayoutSenha.setErrorEnabled(false);
                }
            }
        });

        btnEntrar.setOnClickListener(v -> ValidarLogin(handler));
        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }

    private void ValidarLogin(Handler handler) {
        if (edtLogin.getText().toString().equals("") || edtSenha.getText().toString().equals("")) {
            if (edtLogin.getText().toString().equals("")) {
                Util.AlertaInfo(this, "ERRO - LOGIN", "Verifique o login.");
                edtLogin.requestFocus();
            } else if (edtSenha.getText().toString().equals("")) {
                Util.AlertaInfo(this, "ERRO - SENHA", "Verifique a senha.");
                edtSenha.requestFocus();
            }
        } else {
            if (textLayoutLogin.isErrorEnabled() || textLayoutSenha.isErrorEnabled()) {
                if (textLayoutLogin.isErrorEnabled()) {
                    Util.AlertaInfo(this, "ERRO - LOGIN", "Verifique o login.");
                    edtLogin.requestFocus();
                } else if (textLayoutSenha.isErrorEnabled()) {
                    Util.AlertaInfo(this, "ERRO - SENHA", "Verifique a senha.");
                    edtSenha.requestFocus();
                }
            } else {
                String login = edtLogin.getText().toString();
                String senha = edtSenha.getText().toString();

                // if (chk.isChecked()) {
                    PreferenciasShared preferenciasShared = new PreferenciasShared(LoginActivity.this);
                    preferenciasShared.salvarDados(login, senha);
                // }

                handler.post(() -> inicializaProgressDialog("LOGIN", "Realizando login."));

                firebaseAuth.signInWithEmailAndPassword(login, senha).addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
                        abrirTelaPrincipal(login, handler);
                    } else {
                        handler.post(() -> {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        });

                        if (command.getException().getClass().equals(FirebaseAuthInvalidUserException.class)) {
                            Util.AlertaInfo(LoginActivity.this, "USUÁRIO", "Usuário não cadastrado.");
                        } else if (command.getException().getClass().equals(FirebaseAuthInvalidCredentialsException.class)) {
                            Util.AlertaInfo(LoginActivity.this, "USUÁRIO", "Senha incorreta.");
                        } else {
                            Util.AlertaInfo(LoginActivity.this, "USUÁRIO", "Login ou senha incorreto(s).\n\n" +
                                                                                                 "Erro (" + Objects.requireNonNull(command.getException()).getClass() + "): " +
                                                                                                 Objects.requireNonNull(command.getException()).getMessage());
                        }
                    }
                });
            }
        }
    }

    private void abrirTelaPrincipal(String login, Handler handler) {
        String idUsuario = Base64Custom.codificarBase64(login);

        databaseReference = ConfiguracaoFirebase.getFirebase()
                                                .child(Nomes.getChaveUsuario())
                                                .child(idUsuario);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);

                    PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
                    preferencias.salvarUsuario(usuario, Base64Custom.codificarBase64(usuario.getEmail()));

                    handler.post(() -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    });

                    enableTela(false);

                    Util.salvarLog(null, idUsuario, "Login realizado.");



                    Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    handler.post(() -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    });

                    if (firebaseAuth.getCurrentUser() != null) {
                        Util.salvarLog(null, null, "Tentativa de logar com o e-mail: " + login);
                        firebaseAuth.signOut();
                    }

                    Util.AlertaInfo(LoginActivity.this, "USUÁRIO", "Usuário não encontrado.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inicializaProgressDialog(String title, String message) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        progressDialog = Util.inicializaProgressDialog(this, title, message);

        progressDialog.show();
    }

    @AfterPermissionGranted(0)
    private void solicitarPermissoes() {
        String[] perms;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            perms = new String[]{Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        } else {
            perms = new String[]{Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        }
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "Permissão de uso de internet.",0 , perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void enableTela(boolean enable){
        edtLogin.setEnabled(enable);
        edtSenha.setEnabled(enable);

        btnEntrar.setEnabled(enable);
        btnCadastrar.setEnabled(enable);
    }
}
