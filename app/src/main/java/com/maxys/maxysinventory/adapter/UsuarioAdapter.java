package com.maxys.maxysinventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Usuario;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class UsuarioAdapter extends ArrayAdapter<Usuario> {

    private List<Usuario> usuarios;
    private Context context;

    public UsuarioAdapter(@NonNull Context context, @NonNull List<Usuario> objects) {
        super(context, 0, objects);
        this.context = context;
        this.usuarios = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;

        if (usuarios != null) {
            if (!usuarios.isEmpty()) {
                Usuario usuario = usuarios.get(position);

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_usuario, parent, false);

                AppCompatTextView tvNome = view.findViewById(R.id.tv_usuario_nome);
                AppCompatTextView tvEmail = view.findViewById(R.id.tv_usuario_email);

                tvNome.setText(usuario.getNome());
                tvEmail.setText(usuario.getEmail());
            }
        }

        return view;
    }
}
