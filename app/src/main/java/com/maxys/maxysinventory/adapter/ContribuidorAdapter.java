package com.maxys.maxysinventory.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.LogAcoes;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.PreferenciasShared;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

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

                ibRemover.setOnClickListener(v -> {
                    DatabaseReference reference = ConfiguracaoFirebase.getFirebase();

                    reference.child("empresa_contribuidores")
                             .child(idEmpresa)
                             .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.getValue() != null) {
                                        if (dataSnapshot.getChildrenCount() <= 1) {
                                            AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                            alert.setTitle("EMPRESA");
                                            alert.setMessage("Você é o último contribuidor desta empresa, deseja realmente ser removido?\nAo fazer isso a empresa será excluída.");
                                            alert.setCancelable(true);
                                            alert.setPositiveButton("Sim", (dialog, which) -> {
                                                removerContribuidores(reference, idUsuarioLogado, contribuidor);
                                                removerEmpresa(reference, idUsuarioLogado, idEmpresa);

                                                // fechar tela
                                                ((Activity) context).finish();
                                            });
                                            alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                            alert.show();
                                        } else {
                                            if (idUsuarioLogado.equals(Base64Custom.codificarBase64(contribuidor.getEmail()))) {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                                alert.setTitle("EMPRESA");
                                                alert.setMessage("Deseja realmente sair da empresa?");
                                                alert.setCancelable(true);
                                                alert.setPositiveButton("Sim", (dialog, which) -> {
                                                    removerContribuidores(reference, idUsuarioLogado, contribuidor);

                                                    // fechar tela
                                                    ((Activity) context).finish();
                                                });
                                                alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                                alert.show();
                                            } else {
                                                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                                alert.setTitle("EMPRESA");
                                                alert.setMessage("Dseja realmente remover o contribuidor?");
                                                alert.setCancelable(true);
                                                alert.setPositiveButton("Sim", (dialog, which) -> {
                                                    removerContribuidores(reference, idUsuarioLogado, contribuidor);
                                                });
                                                alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                                alert.show();
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
