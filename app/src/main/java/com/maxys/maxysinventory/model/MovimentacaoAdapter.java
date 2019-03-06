package com.maxys.maxysinventory.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.maxys.maxysinventory.R;

import java.util.List;

public class MovimentacaoAdapter extends RecyclerView.Adapter<MovimentacaoAdapter.MovimentacaoViewHolder> {

    private List<Produto> produtos;

    public MovimentacaoAdapter(List<Produto> produtos) {
        this.produtos = produtos;
    }

    protected static class MovimentacaoViewHolder extends RecyclerView.ViewHolder {
        TextView edtProdutoDescricao;
        TextView edtProdutoSaldo;
        TextView edtProdutoAvariados;

        MovimentacaoViewHolder(View itemView) {
            super(itemView);
            edtProdutoDescricao = itemView.findViewById(R.id.tv_movimentacao_produto);
            edtProdutoSaldo = itemView.findViewById(R.id.tv_movimentacao_saldo);
            edtProdutoAvariados = itemView.findViewById(R.id.tv_movimentacao_avariado);
        }
    }

    @NonNull
    @Override
    public MovimentacaoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycle_movimentacao, viewGroup, false);
        return new MovimentacaoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MovimentacaoViewHolder movimentacaoViewHolder, int i) {
        String descricao = produtos.get(i).getCodReferencia() + " - " + produtos.get(i).getDescricao();
        String saldo = "Saldo: " + String.valueOf(produtos.get(i).getSaldo());
        String avariados = "Avariados: " + String.valueOf(produtos.get(i).getAvariados());

        movimentacaoViewHolder.edtProdutoDescricao.setText(descricao);
        movimentacaoViewHolder.edtProdutoSaldo.setText(saldo);
        movimentacaoViewHolder.edtProdutoAvariados.setText(avariados);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

}
