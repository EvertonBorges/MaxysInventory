package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
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
import com.maxys.maxysinventory.util.Preferencias;
import com.maxys.maxysinventory.util.Util;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private AppCompatEditText edtLogin;
    private AppCompatEditText edtSenha;

    private TextInputLayout textLayoutLogin;
    private TextInputLayout textLayoutSenha;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = ConfiguracaoFirebase.getFirebaseAuth();
        if (firebaseAuth.getCurrentUser() != null) {
            abrirTelaPrincipal(firebaseAuth.getCurrentUser().getEmail());
        }

        Button btnEntrar = findViewById(R.id.bt_login_logar);
        Button btnCadastrar = findViewById(R.id.bt_login_cadastrar);

        edtLogin = findViewById(R.id.edtLogin);
        edtSenha = findViewById(R.id.edtSenha);

        textLayoutLogin = findViewById(R.id.textLayoutCreateLogin);
        textLayoutSenha = findViewById(R.id.textLayoutCreateSenha);

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

        btnEntrar.setOnClickListener(v -> ValidarLogin());
        btnCadastrar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }

    private void ValidarLogin() {
        if (edtLogin.getText().toString().equals("") || edtLogin.getText().toString().equals("")) {
            if (edtLogin.getText().toString().equals("")) {
                Util.AlertaInfo(this, "ERRO - LOGIN", "Verifique o login.");
                edtLogin.requestFocus();
            } else if (edtLogin.getText().toString().equals("")) {
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
                String email = edtLogin.getText().toString();
                String senha = edtSenha.getText().toString();
                firebaseAuth.signInWithEmailAndPassword(email, senha).addOnCompleteListener(command -> {
                    if (command.isSuccessful()) {
                        abrirTelaPrincipal(email);
                    }
                });
            }
        }
    }

    private void abrirTelaPrincipal(String email) {
        String idUsuario = Base64Custom.codificarBase64(email);

        databaseReference = ConfiguracaoFirebase.getFirebase()
                                                .child(Nomes.getChaveUsuario())
                                                .child(idUsuario);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Usuario usuario = dataSnapshot.getValue(Usuario.class);

                    Preferencias preferencias = new Preferencias(LoginActivity.this);
                    preferencias.salvarDados(Base64Custom.codificarBase64(Objects.requireNonNull(usuario).getEmail()), usuario.getNome(), usuario.isAdmin());

                    Intent intent = new Intent(LoginActivity.this, PrincipalActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
