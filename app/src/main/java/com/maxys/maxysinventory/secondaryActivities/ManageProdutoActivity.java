package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.ProdutoAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.ComportamentoTelaProduto;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Inventario;
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

    private AppCompatEditText edtCodReferencia;
    private AppCompatEditText edtDescricao;
    private AppCompatEditText edtPesquisa;

    private Query query;
    private ValueEventListener valueEventListener;

    private Empresa empresa;
    private String idUsuarioLogado;

    private String[] permissoesNecessarias = new String[] { Manifest.permission.INTERNET };

    private ArrayAdapter adapter;
    private List<Produto> produtos;

    private static Produto produtoOld;
    private static ComportamentoTelaProduto comportamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_produto);

        produtos = new ArrayList<>();

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        idUsuarioLogado = preferencias.getIdUsuarioLogado();
        empresa = (Empresa) getIntent().getSerializableExtra("empresa");

        TextInputLayout textLayoutCodReferencia = findViewById(R.id.ti_manage_produto_cod_ref);
        TextInputLayout textLayoutDescricao = findViewById(R.id.ti_manage_produto_descricao);
        edtCodReferencia = findViewById(R.id.et_manage_produto_cod_ref);
        edtDescricao = findViewById(R.id.et_manage_produto_descricao);
        edtPesquisa = findViewById(R.id.et_manage_produto_pesquisa);
        AppCompatTextView tvProdutoQtde = findViewById(R.id.tv_manage_produto_qtde);
        View divider = findViewById(R.id.divider4);

        ImageButton btnBarcode = findViewById(R.id.btn_manage_produto_barcode);

        LinearLayout linearLayout = findViewById(R.id.ll_5);
        ImageButton btnLimpar = findViewById(R.id.btn_manage_produto_limpar);
        ImageButton btnSalvar = findViewById(R.id.btn_manage_produto_salvar);
        ImageButton btnPesquisa = findViewById(R.id.ib_manage_produto_pesquisa);

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

        adapter = new ProdutoAdapter(ManageProdutoActivity.this, produtos, empresa.getId(), permitirRemoverProduto, permitirAlterarProduto);
        lvProdutos.setAdapter(adapter);

        boolean permitirEscritaCampos = permitirAdicionarProduto || permitirAlterarProduto;

        int visibilidadeComponentes = permitirEscritaCampos ? View.VISIBLE: View.GONE;

        textLayoutCodReferencia.setVisibility(visibilidadeComponentes);
        textLayoutDescricao.setVisibility(visibilidadeComponentes);
        linearLayout.setVisibility(visibilidadeComponentes);
        btnBarcode.setVisibility(visibilidadeComponentes);
        btnLimpar.setVisibility(visibilidadeComponentes);
        btnSalvar.setVisibility(visibilidadeComponentes);
        divider.setVisibility(visibilidadeComponentes);

        query = ConfiguracaoFirebase.getFirebase()
                                    .child("empresa_produtos")
                                    .child(empresa.getId());

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                produtos.clear();

                if (dataSnapshot.getValue() != null) {
                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                        Produto produto = snapshot.getValue(Produto.class);
                        produtos.add(produto);
                    }
                }

                String qtdeProdutos = "Total de produtos: " + produtos.size();
                tvProdutoQtde.setText(qtdeProdutos);

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

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
            } else {
                if (comportamento.equals(ComportamentoTelaProduto.INSERT)) {
                    DatabaseReference reference = ConfiguracaoFirebase.getFirebase();

                    reference.child("empresa_produtos")
                        .child(empresa.getId())
                        .orderByChild("codReferencia")
                        .equalTo(produto.getCodReferencia())
                        .limitToFirst(1)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {
                                    AlertDialog.Builder alert = Util.Alerta(ManageProdutoActivity.this, "ALTERAR PRODUTO", "Deseja realmente alterar o produto?");
                                    alert.setCancelable(true);
                                    alert.setPositiveButton("Sim", (dialog, which) -> {
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
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
                                    reference.child("empresa_produtos")
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
                } else if (comportamento.equals(ComportamentoTelaProduto.EDIT)) {
                    DatabaseReference reference = ConfiguracaoFirebase.getFirebase();

                    reference.child("empresa_produtos")
                        .child(empresa.getId())
                        .orderByChild("codReferencia")
                        .equalTo(produto.getCodReferencia())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                // Produto com o novo cód de referências já existe
                                if (dataSnapshot.getValue() != null) {
                                    Util.AlertaInfo(ManageProdutoActivity.this,"PRODUTO JÁ EXISTE", "Por favor verificar o produto, já existe outro produto com o mesmo código.");
                                } else {
                                    reference.child("inventario")
                                        .child(empresa.getId())
                                        .orderByChild("codReferencia")
                                        .equalTo(produtoOld.getCodReferencia())
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                // Verifica se produto possui inventário.
                                                if (dataSnapshot.getValue() != null) {
                                                    for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                        Inventario inventario = snapshot.getValue(Inventario.class);

                                                        inventario.setCodReferencia(produto.getCodReferencia());
                                                        inventario.setDescricao(produto.getDescricao());

                                                        snapshot.getRef().setValue(inventario);
                                                    }
                                                }

                                                reference.child("empresa_produtos")
                                                    .child(empresa.getId())
                                                    .orderByChild("codReferencia")
                                                    .equalTo(produtoOld.getCodReferencia())
                                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.getValue() != null) {
                                                                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                                                    snapshot.getRef().setValue(produto).addOnCompleteListener(task -> {
                                                                        if (task.isSuccessful()) {
                                                                            Util.salvarLog(empresa.getId(), idUsuarioLogado, "Produto '" + snapshot.getKey() + "' atualizado com sucesso!");
                                                                            Util.AlertaInfo(ManageProdutoActivity.this, "PRODUTO ATUALIZADO", "Produto atualizado com sucesso!");
                                                                            limparCampos();
                                                                        } else {
                                                                            Util.salvarLog(empresa.getId(), idUsuarioLogado, "Falha ao atualizar o produto '" + snapshot.getKey() + "'");
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                                        }
                                                    });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    reference.child("empresa_movimentacoes")
                        .child(empresa.getId())
                        .orderByChild("codReferencia")
                        .equalTo(produto.getCodReferencia())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.getValue() != null) {

                                } else {

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                }
            }
        });

        btnPesquisa.setOnClickListener(v -> {
            String textoPesquisa = edtPesquisa.getText().toString();

            if (textoPesquisa.isEmpty()) {
                query.removeEventListener(valueEventListener);

                query = ConfiguracaoFirebase.getFirebase()
                                            .child("empresa_produtos")
                                            .child(empresa.getId());

                valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        produtos.clear();

                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                Produto produto = snapshot.getValue(Produto.class);
                                produtos.add(produto);
                            }
                        }

                        String qtdeProdutos = "Total de produtos: " + produtos.size();
                        tvProdutoQtde.setText(qtdeProdutos);

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                query.addValueEventListener(valueEventListener);
            } else {
                query.removeEventListener(valueEventListener);

                query = ConfiguracaoFirebase.getFirebase()
                                            .child("empresa_produtos")
                                            .child(empresa.getId())
                                            .orderByChild("descricao")
                                            .startAt(textoPesquisa)
                                            .endAt(textoPesquisa + "\uf8ff");

                valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        produtos.clear();

                        if (dataSnapshot.getValue() != null) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                Produto produto = snapshot.getValue(Produto.class);
                                produtos.add(produto);
                            }

                            String qtdeProdutos = "Total de produtos: " + produtos.size();
                            tvProdutoQtde.setText(qtdeProdutos);
                        } else {
                            query.removeEventListener(valueEventListener);

                            query = ConfiguracaoFirebase.getFirebase()
                                                        .child("empresa_produtos")
                                                        .child(empresa.getId())
                                                        .orderByChild("codReferencia")
                                                        .startAt(textoPesquisa)
                                                        .endAt(textoPesquisa + "\uf8ff");

                            valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    produtos.clear();

                                    if (dataSnapshot.getValue() != null) {
                                        for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                            Produto produto = snapshot.getValue(Produto.class);
                                            produtos.add(produto);
                                        }

                                        String qtdeProdutos = "Total de produtos: " + produtos.size();
                                        tvProdutoQtde.setText(qtdeProdutos);
                                    } else {
                                        String qtdeProdutos = "Total de produtos: " + 0;
                                        tvProdutoQtde.setText(qtdeProdutos);
                                    }

                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };

                            query.addValueEventListener(valueEventListener);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                };

                query.addValueEventListener(valueEventListener);
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
        comportamento = ComportamentoTelaProduto.INSERT;
    }

    @Override
    protected void onStart() {
        super.onStart();

        query.addValueEventListener(valueEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        query.removeEventListener(valueEventListener);
    }

    public static void setComportamento(ComportamentoTelaProduto comportamento, Produto produtoOld) {
        ManageProdutoActivity.comportamento = comportamento;
        ManageProdutoActivity.produtoOld = produtoOld;
    }

}