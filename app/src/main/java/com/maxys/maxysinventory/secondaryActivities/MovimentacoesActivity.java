package com.maxys.maxysinventory.secondaryActivities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.MovimentacaoAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Movimentacao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MovimentacoesActivity extends AppCompatActivity {

    private MovimentacaoAdapter adapter;
    private List<Movimentacao> movimentacoes;

    private Query query;
    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimentacoes);

        Intent intent = getIntent();
        String idProduto = Objects.requireNonNull(intent.getExtras()).getString("idProduto");
        String codReferencia = Objects.requireNonNull(intent.getExtras()).getString("codReferencia");
        String descricao = Objects.requireNonNull(intent.getExtras()).getString("descricao");
        String idEmpresa = Objects.requireNonNull(intent.getExtras()).getString("idEmpresa");

        movimentacoes = new ArrayList<>();

        AppCompatTextView tvProduto = findViewById(R.id.tv_movimentacao_titulo);
        String titulo = codReferencia + "\n" + descricao;
        tvProduto.setText(titulo);

        ListView listView = findViewById(R.id.lv_movimentacoes);
        adapter = new MovimentacaoAdapter(MovimentacoesActivity.this, movimentacoes);
        listView.setAdapter(adapter);

        query = ConfiguracaoFirebase.getFirebase()
                                    .child("empresa_movimentacoes")
                                    .child(Objects.requireNonNull(idEmpresa))
                                    .orderByChild("idProduto")
                                    .equalTo(idProduto);

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                movimentacoes.clear();

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        Movimentacao movimentacao = snapshot.getValue(Movimentacao.class);

                        movimentacoes.add(movimentacao);
                    }
                }

                Collections.sort(movimentacoes);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        query.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        query.removeEventListener(valueEventListener);
    }
}
