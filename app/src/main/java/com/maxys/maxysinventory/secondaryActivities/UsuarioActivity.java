package com.maxys.maxysinventory.secondaryActivities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.UsuarioAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class UsuarioActivity extends AppCompatActivity {

    private UsuarioAdapter adapter;
    private List<Usuario> usuarios;

    private DatabaseReference reference = ConfiguracaoFirebase.getFirebase().child("usuarios");
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuario);

        usuarios = new ArrayList<>();

        ListView listViewUsuarios = findViewById(R.id.lv_usuarios);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usuarios.clear();

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        Usuario usuario = snapshot.getValue(Usuario.class);

                        usuarios.add(usuario);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        adapter = new UsuarioAdapter(UsuarioActivity.this, usuarios);
        listViewUsuarios.setAdapter(adapter);

        listViewUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            Usuario usuario = usuarios.get(position);

            Intent intent = new Intent(UsuarioActivity.this, PermissaoActivity.class);
            intent.putExtra("idUsuarioSelecionado", Base64Custom.codificarBase64(usuario.getEmail()));
            intent.putExtra("tipoSelecaoPermissao", TipoSelecaoPermissao.GERAL.toString());
            intent.putExtra("usuario", usuario);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        reference.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        reference.removeEventListener(valueEventListener);
    }
}
