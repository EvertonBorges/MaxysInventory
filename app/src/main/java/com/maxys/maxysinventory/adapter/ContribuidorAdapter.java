package com.maxys.maxysinventory.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.TipoSelecaoPermissao;
import com.maxys.maxysinventory.secondaryActivities.PermissaoActivity;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

public class ContribuidorAdapter extends ArrayAdapter<Contribuidor> {

    private Context context;
    private List<Contribuidor> contribuidores;
    private String idEmpresa;

    public ContribuidorAdapter(Context context, List<Contribuidor> objects, String idEmpresa) {
        super(context, 0, objects);
        this.context = context;
        this.contribuidores = objects;
        this.idEmpresa = idEmpresa;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (contribuidores != null) {
            if (!contribuidores.isEmpty()) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_contribuidor, parent, false);

                TextView nome = view.findViewById(R.id.tv_contribuidor_nome);
                TextView email = view.findViewById(R.id.tv_contribuidor_email);

                ImageButton ibRemover = view.findViewById(R.id.ib_contribuidor_remover);

                Contribuidor contribuidor = contribuidores.get(position);

                nome.setText(contribuidor.getNome());
                email.setText(contribuidor.getEmail());

                PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
                String idUsuarioLogado = preferencias.getIdUsuarioLogado();

                List<String> permissoes = new ArrayList<>();
                for (Permissao permissao: preferencias.getContribuidor().getPermissoes()) {
                    if (!permissoes.contains(permissao.getNome())) {
                        permissoes.add(permissao.getNome());
                    }
                }
                for (Permissao permissao: preferencias.getUsuario().getPermissoes()) {
                    if (!permissoes.contains(permissao.getNome())) {
                        permissoes.add(permissao.getNome());
                    }
                }

                boolean permitirPermissoesEmpresa = permissoes.contains("actPermissoesEmpresa");
                boolean permitirPermissoesGerais = permissoes.contains("actPermissoesGerais");

                view.setOnClickListener(v -> {
                    if (permitirPermissoesEmpresa || permitirPermissoesGerais) {
                        Intent intent = new Intent(context, PermissaoActivity.class);
                        intent.putExtra("idContribuidorSelecionado", Base64Custom.codificarBase64(contribuidor.getEmail()));
                        intent.putExtra("tipoSelecaoPermissao", TipoSelecaoPermissao.CONTRIBUIDOR.toString());
                        intent.putExtra("contribuidor", contribuidor);
                        intent.putExtra("idEmpresa", idEmpresa);
                        context.startActivity(intent);
                    }
                });

                ibRemover.setOnClickListener(v -> {
                    DatabaseReference reference = ConfiguracaoFirebase.getFirebase();

                    reference.child("empresa_contribuidores")
                             .child(idEmpresa)
                             .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        if (dataSnapshot.getChildrenCount() <= 1) {
                                            Util.Alerta(context, "CONTRIBUIDOR", "Você é o último contribuidor desta empresa, deseja realmente ser removido?\nAo fazer isso a empresa será excluída.", (dialog, which) -> {
                                                removerContribuidores(reference, idUsuarioLogado, contribuidor);
                                                removerEmpresa(reference, idUsuarioLogado, idEmpresa);

                                                ((Activity) context).finish();
                                            }).setNegativeButton("Não", (dialog, which) -> dialog.dismiss()).show();
                                        } else {
                                            if (idUsuarioLogado.equals(Base64Custom.codificarBase64(contribuidor.getEmail()))) {

                                                Util.Alerta(context, "CONTRIBUIDOR", "Deseja realmente sair da empresa?", (dialog, which) -> {
                                                    removerContribuidores(reference, idUsuarioLogado, contribuidor);

                                                    ((Activity) context).finish();
                                                }).setNegativeButton("Não", (dialog, which) -> dialog.dismiss()).show();
                                            } else {
                                                Util.Alerta(context, "CONTRIBUIDOR", "Deseja realmente remover o contribuidor?", (dialog, which) -> {
                                                    removerContribuidores(reference, idUsuarioLogado, contribuidor);
                                                }).setNegativeButton("Não", (dialog, which) -> dialog.dismiss()).show();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                });
            }
        }

        return view;
    }

    private void removerContribuidores(DatabaseReference reference, String idUsuarioLogado, Contribuidor contribuidor) {
        reference.child("empresa_contribuidores")
                .child(idEmpresa)
                .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                .removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (task.isSuccessful()) {
                    Util.salvarLog(idEmpresa, idUsuarioLogado, "Contribuidor '" + contribuidor.getEmail() + "' removido da empresa.");
                } else {
                    Util.salvarLog(idEmpresa, idUsuarioLogado, "Erro ao tentar remover contribuidor '" + contribuidor.getEmail() + "' da empresa.");
                }
            }
        });

        reference.child("contribuidor_empresas")
                .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                .child(idEmpresa)
                .removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Util.salvarLog(idEmpresa, idUsuarioLogado, "Empresa removida do contribuidor '" + contribuidor.getEmail() + "'.");
            } else {
                Util.salvarLog(idEmpresa, idUsuarioLogado, "Erro ao tentar remover empresa do contribuidor '" + contribuidor.getEmail() + "'.");
            }
        });
    }

    private void removerEmpresa(DatabaseReference reference, String idUsuarioLogado, String idEmpresa) {
        reference.child("empresa").child(idEmpresa).removeValue().addOnCompleteListener(command -> {
            if (command.isSuccessful()) {
                Util.salvarLog(idEmpresa, idUsuarioLogado, "Empresa removida.");
            } else {
                Util.salvarLog(idEmpresa, idUsuarioLogado, "Erro ao tentar remover a empresa.");
            }
        });
    }
}
