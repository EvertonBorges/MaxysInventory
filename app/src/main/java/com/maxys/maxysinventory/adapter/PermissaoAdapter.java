package com.maxys.maxysinventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.model.Usuario;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PermissaoAdapter extends ArrayAdapter<Permissao> {

    private final Context context;
    private final List<Permissao> permissoes;
    private final TipoSelecaoPermissao tipoSelecaoPermissao;
    private final Usuario usuario;
    private final Contribuidor contribuidor;

    public PermissaoAdapter(@NonNull Context context, @NonNull List<Permissao> objects, TipoSelecaoPermissao tipoSelecaoPermissao, Usuario usuario, Contribuidor contribuidor) {
        super(context, 0, objects);
        this.context = context;
        this.permissoes = objects;
        this.tipoSelecaoPermissao = tipoSelecaoPermissao;
        this.usuario = usuario;
        this.contribuidor = contribuidor;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;

        if (permissoes != null) {
            if (!permissoes.isEmpty()) {
                Permissao permissao = permissoes.get(position);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_permissoes, parent, false);

                CheckBox checkBox = view.findViewById(R.id.chk_permissao_ativo);
                TextView textView = view.findViewById(R.id.tv_permissao_descricao);

                boolean isPermissaoGeral = tipoSelecaoPermissao.equals(TipoSelecaoPermissao.GERAL);

                view.setOnClickListener(v -> {
                    checkBox.setChecked(!checkBox.isChecked());
                });

                if (isPermissaoGeral) {
                    checkBox.setChecked(usuario.getPermissoes().contains(permissao));
                } else {
                    checkBox.setChecked(contribuidor.getPermissoes().contains(permissao));
                }

                textView.setText(permissao.getDescricao());
            }
        }

        return view;
    }
}
