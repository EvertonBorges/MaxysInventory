package com.maxys.maxysinventory.dao;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.model.Empresa;

import java.util.ArrayList;
import java.util.List;

public class EmpresaDao {

    private final FirebaseDatabase firebaseDatabase;

    public EmpresaDao() {
        this.firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public Task addUpdateEmpresa(Empresa empresa){
        return firebaseDatabase.getReference().child("Empresa").child(empresa.getId()).setValue(empresa);
    }

    public List<Empresa> findEmpresaByNode(String node, String value) {
        final List<Empresa> empresas = new ArrayList<>();
        Query query = firebaseDatabase.getReference().child("Empresa").orderByChild(node).equalTo(value);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    empresas.add(snapshot.getValue(Empresa.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return empresas;
    }

}
