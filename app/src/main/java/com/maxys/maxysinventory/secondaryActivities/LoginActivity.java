package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.maxys.maxysinventory.MainActivity;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.util.Util;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private AppCompatEditText edtLogin;
    private AppCompatEditText edtSenha;

    private TextInputLayout textLayoutLogin;
    private TextInputLayout textLayoutSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        Button btnEntrar = findViewById(R.id.btnEntrar);
        Button btnCadastrar = findViewById(R.id.btnCadastrar);

        edtLogin = findViewById(R.id.edtLogin);
        edtSenha = findViewById(R.id.edtSenha);

        textLayoutLogin = findViewById(R.id.textLayoutCreateLogin);
        textLayoutSenha = findViewById(R.id.textLayoutCreateLogin);

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

            finish();
        });
    }

    private void ValidarLogin() {
        if (textLayoutLogin.isErrorEnabled() || textLayoutSenha.isErrorEnabled()) {
            if (textLayoutLogin.isErrorEnabled()) {
                Util.AlertaInfo(this, "ERRO - LOGIN", "Verifique o login.");
                edtLogin.requestFocus();
            } else if (textLayoutSenha.isErrorEnabled()) {
                Util.AlertaInfo(this, "ERRO - SENHA", "Verifique a senha.");
                edtSenha.requestFocus();
            }
        } else {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(Objects.requireNonNull(edtLogin.getText()).toString(), Objects.requireNonNull(edtSenha.getText()).toString()).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Util.AlertaInfo(LoginActivity.this, "ERRO", "Login ou senha inválidos");
                }
            });
        }
    }
}
