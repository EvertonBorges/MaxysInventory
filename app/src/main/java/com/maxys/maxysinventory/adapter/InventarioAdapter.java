package com.maxys.maxysinventory.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Inventario;
import com.maxys.maxysinventory.util.Util;

import java.text.SimpleDateFormat;
import java.util.List;

public class InventarioAdapter extends ArrayAdapter<Inventario> {

    private Context context;
    private List<Inventario> inventarios;

    public InventarioAdapter(Context context, List<Inventario> objects) {
        super(context, 0, objects);
        this.context = context;
        this.inventarios = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (inventarios != null) {
            if (!inventarios.isEmpty()) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_movimentacao, parent, false);

                TextView descricao = view.findViewById(R.id.tv_item_movimentacao_produto);
                TextView dataHora = view.findViewById(R.id.tv_item_movimentacao_data_hora);
                TextView saldo = view.findViewById(R.id.tv_item_movimentacao_saldo);
                TextView avariado = view.findViewById(R.id.tv_item_movimentacao_avariado);

                Inventario inventario = inventarios.get(position);

                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy - HH:mm:ss");

                descricao.setText(inventario.getCodReferencia() + " - " + inventario.getDescricao());
                dataHora.setText(formatador.format(inventario.getDataHora().getTime()));
                saldo.setText(Util.insereZeros(inventario.getSaldo(), 0, 2));
                avariado.setText(Util.insereZeros(inventario.getAvariados(), 0, 2));
            }
        }

        return view;
    }
}
