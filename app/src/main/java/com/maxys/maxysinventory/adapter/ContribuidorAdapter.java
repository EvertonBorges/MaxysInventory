package com.maxys.maxysinventory.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.LogAcoes;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.util.Base64Custom;
import com.maxys.maxysinventory.util.Preferencias;
import com.maxys.maxysinventory.util.Util;

import java.util.Calendar;
import java.util.List;

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

                Preferencias preferencias = new Preferencias(context);
                String idUsuarioLogado = preferencias.getIdentificador();

                ibRemover.setOnClickListener(v -> {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("REMOVER CONTRIBUIDOR");
                    alert.setMessage("Deseja realmente remover o contribuidor " + contribuidor.getNome() + "?");
                    alert.setCancelable(true);
                    alert.setPositiveButton("Sim", (dialog, which) -> {
                        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase();
                        databaseReference.child("empresa_contribuidores")
                                .child(idEmpresa)
                                .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                .removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Contribuidor removido da empresa com sucesso!", Toast.LENGTH_LONG).show();

                                LogAcoes logAcoes = new LogAcoes();
                                logAcoes.setIdUsuario(idUsuarioLogado);
                                logAcoes.setIdEmpresa(idEmpresa);
                                logAcoes.setDescricao("Removido contribuidor (" + Base64Custom.codificarBase64(contribuidor.getEmail()) + ") para a empresa (" + idEmpresa + ").");
                                logAcoes.salvarLog();
                            }
                        });

                        databaseReference.child("contribuidor_empresas")
                                .child(Base64Custom.codificarBase64(contribuidor.getEmail()))
                                .child(idEmpresa)
                                .removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Empresa removida do contribuidor com sucesso!", Toast.LENGTH_LONG).show();

                                LogAcoes logAcoes = new LogAcoes();
                                logAcoes.setIdUsuario(idUsuarioLogado);
                                logAcoes.setIdEmpresa(idEmpresa);
                                logAcoes.setDescricao("Removida empresa (" + idEmpresa + ") para o contribuidor (" + Base64Custom.codificarBase64(contribuidor.getEmail()) + ").");
                                logAcoes.salvarLog();
                            }
                        });

                        dialog.dismiss();
                    });
                    alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                    alert.show();
                });
            }
        }

        return view;
    }
}
