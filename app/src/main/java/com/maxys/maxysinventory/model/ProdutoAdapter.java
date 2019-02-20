package com.maxys.maxysinventory.model;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxys.maxysinventory.R;

import java.util.List;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {

    private List<Produto> produtos;

    public ProdutoAdapter(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        TextView edtProdutoDescricao;
        TextView edtProdutoSaldo;
        TextView edtProdutoAvariados;

        ProdutoViewHolder(View itemView) {
            super(itemView);
            edtProdutoDescricao = itemView.findViewById(R.id.edtProdutoDescricao);
            edtProdutoSaldo = itemView.findViewById(R.id.edtProdutoSaldo);
            edtProdutoAvariados = itemView.findViewById(R.id.edtProdutoAvariados);
        }
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        ProdutoViewHolder pvh = new ProdutoViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder produtoViewHolder, int i) {
        String descricao = produtos.get(i).getCodReferencia() + " - " + produtos.get(i).getDescricao();
        String saldo = "Saldo: " + String.valueOf(produtos.get(i).getSaldo());
        String avariados = "Avariados: " + String.valueOf(produtos.get(i).getAvariados());

        produtoViewHolder.edtProdutoDescricao.setText(descricao);
        produtoViewHolder.edtProdutoSaldo.setText(saldo);
        produtoViewHolder.edtProdutoAvariados.setText(avariados);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

}
