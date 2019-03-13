package com.maxys.maxysinventory.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.PreferenciasStatic;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PermissaoUsuarioAdapter extends ArrayAdapter<Permissao> {

    private final DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
    private final Context context;
    private final List<Permissao> permissoes;
    private final TipoSelecaoPermissao tipoSelecaoPermissao;
    private final Empresa empresa;

    public PermissaoUsuarioAdapter(@NonNull Context context, @NonNull List<Permissao> objects, Empresa empresa, TipoSelecaoPermissao tipoSelecaoPermissao) {
        super(context, 0, objects);
        this.context = context;
        this.permissoes = objects;
        this.empresa = empresa;
        this.tipoSelecaoPermissao = tipoSelecaoPermissao;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = null;

        if (permissoes != null) {
            if (!permissoes.isEmpty()) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_permissoes, parent, false);

                Permissao permissao = permissoes.get(position);
                PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
                Contribuidor contribuidor = preferencias.getContribuidor();
                String idUsuarioLogado = preferencias.getIdUsuarioLogado();
                Usuario usuario = preferencias.getUsuario();

                CheckBox checkBox = view.findViewById(R.id.chk_permissao_ativo);
                TextView textView = view.findViewById(R.id.tv_permissao_descricao);
                textView.setText(permissao.getDescricao());

                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (tipoSelecaoPermissao.equals(TipoSelecaoPermissao.GERAL)) {
                            if (!usuario.getPermissoes().contains(permissao)) {
                                usuario.getPermissoes().add(permissao);

                                reference.child("usuarios")
                                         .child(idUsuarioLogado)
                                         .setValue(usuario);
                            }
                        } else {
                            if (!contribuidor.getPermissoes().contains(permissao)) {
                                contribuidor.getPermissoes().add(permissao);

                                reference.child("empresa_contribuidores")
                                        .child(empresa.getId())
                                        .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                        .setValue(contribuidor);
                            }
                        }
                    } else {
                        if (tipoSelecaoPermissao.equals(TipoSelecaoPermissao.GERAL)) {
                            if (usuario.getPermissoes().contains(permissao)) {
                                usuario.getPermissoes().remove(permissao);

                                reference.child("usuarios")
                                        .child(idUsuarioLogado)
                                        .setValue(usuario);
                            }
                        } else {
                            if (contribuidor.getPermissoes().contains(permissao)) {
                                contribuidor.getPermissoes().remove(permissao);

                                reference.child("empresa_contribuidores")
                                        .child(empresa.getId())
                                        .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                        .setValue(contribuidor);
                            }
                        }
                    }
                });
            }
        }

        return view;
    }
}
