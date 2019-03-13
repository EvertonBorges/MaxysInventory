package com.maxys.maxysinventory.adapter;

import android.app.AlertDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
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
import com.maxys.maxysinventory.model.Produto;
import com.maxys.maxysinventory.util.PreferenciasShared;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.List;

public class ProdutoAdapter extends ArrayAdapter<Produto> {

    private Context context;
    private String idEmpresa;
    private List<Produto> produtos;
    private boolean isActRemoverProduto;

    public ProdutoAdapter(Context context, List<Produto> objects, String idEmpresa, boolean isActRemoverProduto) {
        super(context, 0, objects);
        this.context = context;
        this.idEmpresa = idEmpresa;
        this.produtos = objects;
        this.isActRemoverProduto = isActRemoverProduto;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;

        if (produtos != null) {
            if (!produtos.isEmpty()) {
                PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
                String idUsuarioLogado = preferencias.getIdUsuarioLogado();

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.recycle_produto, parent, false);

                TextView tvCodReferencia = view.findViewById(R.id.tv_item_produto_cod_ref);
                TextView tvDescricao = view.findViewById(R.id.tv_item_produto_descricao);

                ImageButton ibEditar = view.findViewById(R.id.ib_item_produto_editar);
                ImageButton ibRemover = view.findViewById(R.id.ib_item_produto_remover);

                ibRemover.setVisibility(isActRemoverProduto ? View.VISIBLE : View.GONE);

                Produto produto = produtos.get(position);
                tvCodReferencia.setText("Cód.: " + produto.getCodReferencia());
                tvDescricao.setText(produto.getDescricao());

                ibEditar.setOnClickListener(v ->  {
                    AppCompatEditText edtCodReferencia = parent.getRootView().findViewById(R.id.et_manage_produto_cod_ref);
                    AppCompatEditText edtDescricao = parent.getRootView().findViewById(R.id.et_manage_produto_descricao);

                    edtCodReferencia.setText(produto.getCodReferencia());
                    edtDescricao.setText(produto.getDescricao());
                });

                ibRemover.setOnClickListener(v -> {
                    DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase();
                    databaseReference.child("inventario")
                                     .child(idEmpresa)
                                     .orderByChild("codReferencia")
                                     .equalTo(produto.getCodReferencia())
                                     .limitToFirst(1)
                                     .addListenerForSingleValueEvent(new ValueEventListener() {
                                         @Override
                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                             if (dataSnapshot.getValue() != null) {
                                                 AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                                 alert.setTitle("REMOVER PRODUTO");
                                                 alert.setMessage("Produto possui movimentações registradas, deseja realmente remove-lo?");
                                                 alert.setCancelable(true);
                                                 alert.setPositiveButton("Sim", (dialog, which) -> {
                                                     for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                         String idProduto = snapshot.getKey();

                                                         snapshot.getRef().removeValue().addOnCompleteListener(command -> {
                                                             // Removido de inventário.
                                                             if (command.isSuccessful()) {
                                                                 Util.salvarLog(idEmpresa, idUsuarioLogado, "Produto " + produto.getCodReferencia() + " removido do inventário.");
                                                             }
                                                         });
                                                         databaseReference.child("empresa_produtos").child(idEmpresa).child(idProduto).removeValue().addOnCompleteListener(command -> {
                                                             // Removido de empresa_produtos
                                                             if (command.isSuccessful()) {
                                                                 Util.salvarLog(idEmpresa, idUsuarioLogado, "Produto " + produto.getCodReferencia() + " removido de empresa_produtos.");
                                                             }
                                                         });
                                                         databaseReference.child("empresa_movimentacoes").child(idEmpresa).orderByChild("idProduto").equalTo(idProduto).addListenerForSingleValueEvent(new ValueEventListener() {
                                                             @Override
                                                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                 if (dataSnapshot.getValue() != null) {
                                                                     for (DataSnapshot snapshot1: dataSnapshot.getChildren()) {
                                                                         snapshot1.getRef().removeValue(); // Remove de empresa_movimentações.
                                                                     }

                                                                     Util.salvarLog(idEmpresa, idUsuarioLogado, "Produto " + produto.getCodReferencia() + " removido de empresa_movimentacoes.");
                                                                 }
                                                             }

                                                             @Override
                                                             public void onCancelled(@NonNull DatabaseError databaseError) {

                                                             }
                                                         });

                                                     }
                                                 });
                                                 alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                                 alert.show();
                                             } else {
                                                 AlertDialog.Builder alert = new AlertDialog.Builder(context);
                                                 alert.setTitle("REMOVER PRODUTO");
                                                 alert.setMessage("Deseja realmente remover o produto?");
                                                 alert.setCancelable(true);
                                                 alert.setPositiveButton("Sim", (dialog, which) -> {
                                                     databaseReference.child("empresa_produtos").child(idEmpresa).orderByChild("codReferencia").equalTo(produto.getCodReferencia()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                         @Override
                                                         public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                             if (dataSnapshot.getValue() != null) {
                                                                 for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                                     snapshot.getRef().removeValue();
                                                                 }

                                                                 Util.salvarLog(idEmpresa, idUsuarioLogado, "Produto " + produto.getCodReferencia() + " removido de empresa_produtos.");
                                                             }
                                                         }

                                                         @Override
                                                         public void onCancelled(@NonNull DatabaseError databaseError) {

                                                         }
                                                     });
                                                 });
                                                 alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                                 alert.show();
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
}
