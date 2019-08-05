package com.maxys.maxysinventory.util;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Usuario;

import java.util.Objects;

import androidx.annotation.NonNull;

public class PreferenciasStatic {

    private static PreferenciasStatic instance;
    private String idUsuarioLogado;
    private Usuario usuario;
    private Contribuidor contribuidor;

    private DatabaseReference referenceUsuario;
    private ValueEventListener valueEventListenerUsuario;

    private PreferenciasStatic() {

    }

    public static synchronized PreferenciasStatic getInstance() {
        if (instance == null) {
            instance = new PreferenciasStatic();
        }

        return instance;
    }

    public void salvarUsuario(Usuario usuario, String idUsuarioLogado) {
        this.usuario = usuario;
        this.idUsuarioLogado = idUsuarioLogado;

        referenceUsuario = ConfiguracaoFirebase.getFirebase().child("usuario").child(idUsuarioLogado);

        valueEventListenerUsuario = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Usuario usuarioFirebase = dataSnapshot.getValue(Usuario.class);

                    usuario.setNome(Objects.requireNonNull(usuarioFirebase).getNome());
                    usuario.setPermissoes(usuarioFirebase.getPermissoes());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        referenceUsuario.addValueEventListener(valueEventListenerUsuario);
    }

    public void salvarContribuidor(Contribuidor contribuidor) {
        this.contribuidor = contribuidor;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getIdUsuarioLogado() {
        return idUsuarioLogado;
    }

    public Contribuidor getContribuidor() {
        return contribuidor;
    }

    public void removerContribuidor() {
        this.contribuidor = null;
    }

    public void removerUsuario() {
        this.usuario = null;
        this.idUsuarioLogado = null;

        referenceUsuario.removeEventListener(valueEventListenerUsuario);
    }

}