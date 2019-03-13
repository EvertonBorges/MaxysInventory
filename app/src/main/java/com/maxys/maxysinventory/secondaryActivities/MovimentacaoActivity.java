package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.adapter.InventarioAdapter;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Movimentacao;
import com.maxys.maxysinventory.model.Produto;
import com.maxys.maxysinventory.model.Inventario;
import com.maxys.maxysinventory.model.TipoRetornoIntent;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MovimentacaoActivity extends AppCompatActivity implements  EasyPermissions.PermissionCallbacks {

    private ToggleButton tgbEstadoMercadoria;
    private EditText edtQtde;
    private EditText edtCodReferencia;
    private ImageButton btnQrCode;
    private Button btnEnviar;
    private CheckBox chkAutoBarcode;
    private EditText edtPesquisa;

    private ListView listView;
    private InventarioAdapter adapter;
    private List<Inventario> inventarioTop10;
    private Empresa empresa;
    private String idUsuarioLogado;

    public static ProgressDialog progressDialog;
    private static Handler handler;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimentacao);

        inventarioTop10 = new ArrayList<>();

        databaseReference = ConfiguracaoFirebase.getFirebase();

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        idUsuarioLogado = preferencias.getIdUsuarioLogado();

        tgbEstadoMercadoria = findViewById(R.id.tb_movimentacao_estado_mercadoria);
        edtQtde = findViewById(R.id.et_movimentacao_qtde);
        edtCodReferencia = findViewById(R.id.et_movimentacao_cod_referencia);
        btnQrCode = findViewById(R.id.ib_movimentacao_barcode);
        btnEnviar = findViewById(R.id.bt_movimentacao_enviar);
        listView = findViewById(R.id.lv_movimentacao);
        chkAutoBarcode = findViewById(R.id.chkAutoBarCode);
        edtPesquisa = findViewById(R.id.et_movimentacao_pesquisa_produto);

        tgbEstadoMercadoria.setOnCheckedChangeListener((buttonView, isChecked) -> tgbEstadoMercadoria.setBackgroundResource(isChecked ? R.color.toggleOn: R.color.toggleOff));

        empresa = (Empresa) getIntent().getSerializableExtra("empresa");

        databaseReference = ConfiguracaoFirebase.getFirebase();

        adapter = new InventarioAdapter(MovimentacaoActivity.this, inventarioTop10);
        listView.setAdapter(adapter);

        databaseReference.child("inventario")
                         .child(empresa.getId())
                         .orderByChild("dataHoraMovimentacao")
                         .limitToLast(10)
                         .addValueEventListener(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                 inventarioTop10.clear();

                                 if (dataSnapshot.getValue() != null) {
                                     for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                         Inventario inventario = snapshot.getValue(Inventario.class);

                                         inventarioTop10.add(inventario);
                                     }
                                 }

                                 adapter.notifyDataSetChanged();
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError databaseError) {

                             }
                         });

        handler = new Handler();

        btnEnviar.setOnClickListener(v -> new Thread() {
            public void run() {
                realizarEnvio();
            }
        }.start());

        btnQrCode.setOnClickListener(v -> cameraPermissao());
    }

    private void abrirCameraIntent() {
        Intent intent = new Intent(this, BarCodeActivity.class);
        startActivityForResult(intent, TipoRetornoIntent.BARCODE_SCAN.ordinal());
    }

    private void realizarEnvio() {
        handler.post(() -> inicializaProgressDialog("PRODUTO", "Validando informações..."));

        int qtde = Integer.parseInt(edtQtde.getText().toString().isEmpty() ? "1" : edtQtde.getText().toString());
        String codReferencia = edtCodReferencia.getText().toString();

        if (qtde == 0) {
            handler.post(() -> {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Util.AlertaInfo(this, "QUANTIDADE", "Quantidade informada não pode ser 0 (zero).");
            });
        } else if (codReferencia.isEmpty()) {
            handler.post(() -> {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                Util.AlertaInfo(this, "CÓD. REFERÊNCIA", "Cód. de referência deve ser informado.");
            });
        } else {
            handler.post(() -> inicializaProgressDialog("REGISTRANDO", "Registrando movimentação..."));

            DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
            reference.child("empresa_produtos")
                     .child(empresa.getId())
                     .orderByChild("codReferencia")
                     .equalTo(codReferencia)
                     .limitToFirst(1)
                     .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            for(DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                String idProduto = snapshot.getKey();
                                Produto produto = snapshot.getValue(Produto.class);

                                try {
                                    boolean isAvariado = !tgbEstadoMercadoria.isChecked();
                                    Movimentacao movimentacao = new Movimentacao();
                                    movimentacao.setQtde(qtde);
                                    movimentacao.setIdProduto(idProduto);
                                    movimentacao.setAvariado(isAvariado);

                                    reference.child("empresa_movimentacoes")
                                             .child(empresa.getId())
                                             .push()
                                             .setValue(movimentacao)
                                             .addOnCompleteListener(task -> {
                                                 if (task.isSuccessful()) {
                                                     Util.salvarLog(empresa.getId(), idUsuarioLogado, "Movimentação registrada para o produto: " + idProduto);

                                                     reference.child("empresa_movimentacoes")
                                                              .child(empresa.getId())
                                                              .orderByChild("idProduto")
                                                              .equalTo(idProduto)
                                                              .addListenerForSingleValueEvent(new ValueEventListener() {
                                                                 @Override
                                                                 public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                     Inventario inventario = new Inventario();
                                                                     inventario.setCodReferencia(produto.getCodReferencia());
                                                                     inventario.setDescricao(produto.getDescricao());

                                                                     if (dataSnapshot.getValue() != null) {
                                                                         for (DataSnapshot snapshot1: dataSnapshot.getChildren()) {
                                                                             Movimentacao m = snapshot1.getValue(Movimentacao.class);
                                                                             if (m.isAvariado()) {
                                                                                 inventario.addAvarias(m.getQtde());
                                                                             } else {
                                                                                 inventario.addSaldo(m.getQtde());
                                                                             }
                                                                         }
                                                                     }

                                                                     reference.child("inventario")
                                                                              .child(empresa.getId())
                                                                              .child(idProduto)
                                                                              .setValue(inventario).addOnCompleteListener(command -> {
                                                                                  if (command.isSuccessful()) {
                                                                                      Util.salvarLog(empresa.getId(), idUsuarioLogado, "Inventário do produto " + idProduto + " foi cadastradi/atualizado.");

                                                                                      handler.post(() -> {
                                                                                          if (progressDialog.isShowing()) {
                                                                                              progressDialog.dismiss();
                                                                                          }

                                                                                          Toast.makeText(MovimentacaoActivity.this, "Movimentação e inventário cadastrados com sucesso.", Toast.LENGTH_LONG).show();
                                                                                          limparCampos();
                                                                                      });
                                                                                  } else {
                                                                                      Util.salvarLog(empresa.getId(), idUsuarioLogado, "Erro ao realizar a atualização/cadastro do inventário para o produto " + idProduto);

                                                                                      handler.post(() -> {
                                                                                          if (progressDialog.isShowing()) {
                                                                                              progressDialog.dismiss();
                                                                                          }

                                                                                          Util.AlertaInfo(MovimentacaoActivity.this, "INVENTÁRIO", "Falha ao salvar o inventário.");
                                                                                      });
                                                                                  }
                                                                     });

                                                                 }

                                                                 @Override
                                                                 public void onCancelled(@NonNull DatabaseError databaseError) {

                                                                 }
                                                             });
                                                 } else {
                                                     handler.post(() -> {
                                                         if (progressDialog.isShowing()) {
                                                             progressDialog.dismiss();
                                                         }

                                                         Util.AlertaInfo(MovimentacaoActivity.this, "MOVIMENTAÇÃO", "Falha ao salvar a movimentação.");
                                                     });
                                                 }
                                             });


                                } catch (Exception ex) {
                                    handler.post(() -> {
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }

                                        Util.AlertaInfo(MovimentacaoActivity.this, "ERRO", "Erro ao enviar as informações.\n\nErro: " + ex.getMessage());
                                        limparCampos();
                                    });
                                }
                            }
                        } else {
                            handler.post(() -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                Util.AlertaInfo(MovimentacaoActivity.this, "PRODUTO NÃO ENCONTRADO", "Produto " + codReferencia + " não encontrado.");

                                limparCampos();
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        handler.post(() -> handler.post(() -> {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }));
                    }
            });
        }
    }

    private void limparCampos() {
        tgbEstadoMercadoria.setChecked(true);
        edtQtde.setText("");
        edtCodReferencia.setText("");
    }

    private void inicializaProgressDialog(String title, String message) {
        if (progressDialog != null) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }

        progressDialog = Util.inicializaProgressDialog(this, title, message);

        progressDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TipoRetornoIntent.BARCODE_SCAN.ordinal()) {
            if (resultCode == RESULT_OK) {
                String barcodeResultado = Objects.requireNonNull(data).getStringExtra("barcodeResultado");
                edtCodReferencia.setText(barcodeResultado);

                if (chkAutoBarcode.isChecked()) {
                    new Thread() {
                        public void run() {
                            realizarEnvio();
                        }
                    }.start();
                }
            }
        } else if (requestCode == TipoRetornoIntent.FILE_SEARCH.ordinal()) {
            if (resultCode == RESULT_OK) {
                String path = Objects.requireNonNull(data).getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                Util.AlertaInfo(MovimentacaoActivity.this, "DADOS ARQUIVO", "Arquivo: " + path + "\nDados:\n" + Util.readFile(path).toString());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @AfterPermissionGranted(0)
    private void cameraPermissao() {
        String[] perms = {Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {
            abrirCameraIntent();
        } else {
            EasyPermissions.requestPermissions(this, "Permissão de uso da câmera.", TipoRetornoIntent.BARCODE_SCAN.ordinal() , perms);
        }
    }


    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (requestCode == TipoRetornoIntent.BARCODE_SCAN.ordinal()) {
            if (perms.contains(Manifest.permission.CAMERA)) {
                abrirCameraIntent();
            }
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == TipoRetornoIntent.BARCODE_SCAN.ordinal()) {
            if (perms.contains(Manifest.permission.CAMERA)) {
                Util.AlertaInfo(MovimentacaoActivity.this,
                                "PERMISSÃO CÂMERA",
                                "É necessário aceitar o uso da câmera para utilizar esta funcionalidade.");
            }
        }
    }

}