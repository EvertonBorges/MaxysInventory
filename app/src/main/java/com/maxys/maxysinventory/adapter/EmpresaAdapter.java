package com.maxys.maxysinventory.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Empresa;

import java.text.SimpleDateFormat;
import java.util.List;

public class EmpresaAdapter extends ArrayAdapter<Empresa> {

    private Context context;
    private List<Empresa> empresas;

    public EmpresaAdapter(Context context, List<Empresa> objects) {
        super(context, 0, objects);
        this.context = context;
        this.empresas = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (empresas != null) {
            if (!empresas.isEmpty()) {
                Empresa empresa = empresas.get(position);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_empresa, parent, false);

                TextView data = view.findViewById(R.id.tv_empresa_data);
                TextView contribuidores = view.findViewById(R.id.tv_empresa_contribuidores);
                TextView nome = view.findViewById(R.id.tv_empresa_nome);

                DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase()
                                                                          .child("empresa_contribuidores")
                                                                          .child(empresa.getId());
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        contribuidores.setText(String.valueOf("Contribuidores: " + dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                data.setText(formatador.format(empresa.getDataHora().getTime()));
                nome.setText(empresa.getNome());
            }
        }

        return view;
    }
}
