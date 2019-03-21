package com.maxys.maxysinventory.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Movimentacao;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class MovimentacaoAdapter extends ArrayAdapter<Movimentacao> {

    private final Context context;
    private final List<Movimentacao> movimentacoes;
    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public MovimentacaoAdapter(@NonNull Context context, @NonNull List<Movimentacao> objects) {
        super(context, 0, objects);
        this.context = context;
        this.movimentacoes = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;

        if (movimentacoes != null) {
            if (!movimentacoes.isEmpty()) {
                Movimentacao movimentacao = movimentacoes.get(position);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_movimentacao, parent, false);

                AppCompatTextView tvDataHora = view.findViewById(R.id.tv_movimentacao_data_hora);
                AppCompatTextView tvEmail = view.findViewById(R.id.tv_movimentacao_usuario_email);
                AppCompatTextView tvNome = view.findViewById(R.id.tv_movimentacao_usuario_nome);
                AppCompatTextView tvQtde = view.findViewById(R.id.tv_movimentacao_qtde);

                String data = "Data: " + formatador.format(movimentacao.getDataHora().getTime());

                tvDataHora.setText(data);
                tvEmail.setText(movimentacao.getEmailUsuario());
                tvNome.setText(movimentacao.getNomeUsuario());
                tvQtde.setText(String.valueOf(movimentacao.getQtde()));

                if (movimentacao.isAvariado()) {
                    tvQtde.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
                } else {
                    tvQtde.setTextColor(context.getResources().getColor(android.R.color.holo_blue_dark));
                }

            }
        }

        return view;
    }

}