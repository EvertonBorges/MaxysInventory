package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.ProdutoAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Permissao;
import com.maxys.maxysinventory.model.Produto;
import com.maxys.maxysinventory.model.TipoRetornoIntent;

import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ManageProdutoActivity extends AppCompatActivity {

    private TextInputLayout textLayoutCodReferencia;
    private TextInputLayout textLayoutDescricao;

    private AppCompatEditText edtCodReferencia;
    private AppCompatEditText edtDescricao;

    private DatabaseReference databaseReference;

    private Empresa empresa;
    private String idUsuarioLogado;

    private String[] permissoesNecessarias = new String[] { Manifest.permission.INTERNET };

    private ArrayAdapter adapter;
    private List<Produto> produtos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_produto);

        produtos = new ArrayList<>();

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        idUsuarioLogado = preferencias.getIdUsuarioLogado();
        empresa = (Empresa) getIntent().getSerializableExtra("empresa");

        edtCodReferencia = findViewById(R.id.et_manage_produto_cod_ref);
        edtDescricao = findViewById(R.id.et_manage_produto_descricao);

        ImageButton btnBarcode = findViewById(R.id.btn_manage_produto_barcode);
        ImageButton btnLimpar = findViewById(R.id.btn_manage_produto_limpar);
        ImageButton btnSalvar = findViewById(R.id.btn_manage_produto_salvar);

        ListView lvProdutos = findViewById(R.id.lv_manage_produto);
        List<String> permissoes = new ArrayList<>();
        for (Permissao permissao: preferencias.getContribuidor().getPermissoes()) {
            if (!permissoes.contains(permissao.getNome())) {
                permissoes.add(permissao.getNome());
            }
        }

        boolean permitirRemoverProduto = permissoes.contains("actRemoverProduto");
        boolean permitirAdicionarProduto = permissoes.contains("actAdicionarProduto");
        boolean permitirAlterarProduto = permissoes.contains("actAlterarProduto");

        adapter = new ProdutoAdapter(ManageProdutoActivity.this, produtos, empresa.getId(), permitirRemoverProduto);
        lvProdutos.setAdapter(adapter);

        databaseReference = ConfiguracaoFirebase.getFirebase();

        databaseReference.child("empresa_produtos")
                         .child(empresa.getId())
                         .addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 produtos.clear();

                                 if (dataSnapshot.getValue() != null) {
                                     for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                         Produto produto = snapshot.getValue(Produto.class);
                                         produtos.add(produto);
                                     }
                                 }

                                 adapter.notifyDataSetChanged();
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         });

        btnBarcode.setOnClickListener(v -> {


            //if (permitido) {
                Intent intent = new Intent(ManageProdutoActivity.this, BarCodeActivity.class);
                startActivityForResult(intent, TipoRetornoIntent.BARCODE_SCAN.ordinal());
                /*
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
                dialog.setTitle("Permissão Câmera");
                dialog.setMessage("É necessário aceitar o uso da câmera para utilizar esta funcionalidade.");
                dialog.setCancelable(true);
                dialog.show();
            }
            */
        });

        btnLimpar.setOnClickListener(v -> {
            limparCampos();
        });

        btnSalvar.setOnClickListener(v -> {
            Produto produto = new Produto();
            produto.setCodReferencia(edtCodReferencia.getText().toString());
            produto.setDescricao(edtDescricao.getText().toString());

            if (produto.getCodReferencia().isEmpty()) {
                Util.AlertaInfo(ManageProdutoActivity.this, "ERRO CÓD. REF.", "Insira o cód de referência do produto.");
            } else if (produto.getDescricao().isEmpty()) {
                Util.AlertaInfo(ManageProdutoActivity.this, "ERRO DESCRIÇÃO.", "Insira a descrição do produto.");
            } else if (produto.getCodReferencia().length() < 8) {
                Util.AlertaInfo(ManageProdutoActivity.this, "ERRO CÓD. REF.", "Cód. de referência de possuir pelo menos 8 números");
            } else {
                DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase();
                databaseReference.child("empresa_produtos")
                                 .child(empresa.getId())
                                 .orderByChild("codReferencia")
                                 .equalTo(produto.getCodReferencia())
                                 .limitToFirst(1)
                                 .addListenerForSingleValueEvent(new ValueEventListener() {
                                     @Override
                                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                         if (dataSnapshot.getValue() != null) {
                                             AlertDialog.Builder alert = new AlertDialog.Builder(ManageProdutoActivity.this);
                                             alert.setTitle("ALTERAR PRODUTO");
                                             alert.setMessage("Deseja realmente alterar o produto?");
                                             alert.setCancelable(true);
                                             alert.setPositiveButton("Sim", (dialog, which) -> {
                                                 for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                     snapshot.getRef()
                                                             .setValue(produto)
                                                             .addOnCompleteListener(task -> {
                                                                 if (task.isSuccessful()) {
                                                                     Util.AlertaInfo(ManageProdutoActivity.this, "PRODUTO ALTERADO", "Produto alterado com sucesso.");
                                                                     Util.salvarLog(empresa.getId(), idUsuarioLogado, "Produto (" + snapshot.getKey() + ") alterado com sucesso.");
                                                                     limparCampos();
                                                                     dialog.dismiss();
                                                                 } else {
                                                                     Util.AlertaInfo(ManageProdutoActivity.this, "ERRO PRODUTO", "Erro ao alterar o produto.");
                                                                     dialog.dismiss();
                                                                 }
                                                             });
                                                 }
                                             });
                                             alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                                             alert.show();
                                         } else {
                                             databaseReference.child("empresa_produtos")
                                                              .child(empresa.getId())
                                                              .push().setValue(produto).addOnCompleteListener(command -> {
                                                                  if (command.isSuccessful()) {
                                                                      Util.AlertaInfo(ManageProdutoActivity.this, "PRODUTO CADASTRADO", "Produto cadastrado com sucesso.");
                                                                      Util.salvarLog(empresa.getId(), idUsuarioLogado, "Produto (" + produto.getCodReferencia() + ") cadastrado com sucesso.");
                                                                      limparCampos();
                                                                  } else {
                                                                      Util.AlertaInfo(ManageProdutoActivity.this, "ERRO PRODUTO", "Erro ao cadastrar o produto.");
                                                                  }
                                             });
                                         }
                                     }

                                     @Override
                                     public void onCancelled(@NonNull DatabaseError databaseError) {

                                     }
                                 });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Arrays.equals(permissions, permissoesNecessarias)) {
            for (int resultado : grantResults) {
                if (resultado == PackageManager.PERMISSION_DENIED) {
                    alertaValidacaoPermissao();
                }
            }
        }
    }

    private void alertaValidacaoPermissao() {
        Util.AlertaInfo(this, "Permissões negadas", "Para utilizar esse app, é necessário aceitar as permissões",
                ((dialog, which) -> finish()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TipoRetornoIntent.BARCODE_SCAN.ordinal()) {
            if (resultCode == RESULT_OK) {
                String barcodeResultado = Objects.requireNonNull(data).getStringExtra("barcodeResultado");
                edtCodReferencia.setText(barcodeResultado);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void limparCampos() {
        edtCodReferencia.setText("");
        edtDescricao.setText("");
    }

}
