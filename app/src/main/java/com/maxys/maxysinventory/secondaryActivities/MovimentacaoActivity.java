package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Movimentacao;
import com.maxys.maxysinventory.model.Produto;
import com.maxys.maxysinventory.model.TipoRetornoIntent;
import com.maxys.maxysinventory.util.Permissao;
import com.maxys.maxysinventory.util.Util;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

public class MovimentacaoActivity extends AppCompatActivity {

    private ConstraintLayout vwPrincipal;

    private ToggleButton tgbEstadoMercadoria;
    private EditText edtQtde;
    private EditText edtCodReferencia;
    private Button btnQrCode;
    private Button btnEnviar;
    private RecyclerView rcvInventario;
    private CheckBox chkAutoBarcode;
    private EditText edtPesquisa;

    private Button btnFileChooser;

    private HashMap<String, Produto> produtos = new HashMap<>();
    private List<Produto> produtosFiltro = new ArrayList<>();
    //private MovimentacaoAdapter adapterProdutos;

    public static ProgressDialog progressDialog;
    private static Handler handler;

    private DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String[] permissoesNecessarias = new String[] { Manifest.permission.INTERNET /*, Manifest.permission.WRITE_EXTERNAL_STORAGE*/ };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimentacao);

        Permissao.validaPermissao(1,this, permissoesNecessarias);

        databaseReference = ConfiguracaoFirebase.getFirebase();

        vwPrincipal = findViewById(R.id.vwPrincipal);
        tgbEstadoMercadoria = findViewById(R.id.tgbEstadoMercadoria);
        edtQtde = findViewById(R.id.edtQtde);
        edtCodReferencia = findViewById(R.id.edtCodReferencia);
        btnQrCode = findViewById(R.id.btnBarCode);
        btnEnviar = findViewById(R.id.btnEnviar);
        rcvInventario = findViewById(R.id.rcvInventario);
        chkAutoBarcode = findViewById(R.id.chkAutoBarCode);
        edtPesquisa = findViewById(R.id.edtPesquisa);

        btnFileChooser = findViewById(R.id.btnFileChooser);

        rcvInventario.setLayoutManager(new LinearLayoutManager(this));

        vwPrincipal.setVisibility(View.GONE);

        if (firebaseAuth.getCurrentUser() != null) {
            vwPrincipal.setVisibility(View.VISIBLE);
        } else {
            onBackPressed();
        }

        tgbEstadoMercadoria.setOnCheckedChangeListener((buttonView, isChecked) -> tgbEstadoMercadoria.setBackgroundResource(isChecked ? R.color.toggleOn: R.color.toggleOff));

        SimpleMaskFormatter simpleMaskQtde = new SimpleMaskFormatter("NNN");
        MaskTextWatcher maskTextQtde = new MaskTextWatcher(edtQtde, simpleMaskQtde);
        edtQtde.addTextChangedListener(maskTextQtde);

        SimpleMaskFormatter simpleMaskCodReferencia = new SimpleMaskFormatter("NNNNNNNNNNNNN");
        MaskTextWatcher maskTextCodReferencia = new MaskTextWatcher(edtCodReferencia, simpleMaskCodReferencia);
        edtCodReferencia.addTextChangedListener(maskTextCodReferencia);

        eventoDatabase();

        handler = new Handler();

        btnEnviar.setOnClickListener(v -> new Thread() {
            public void run() {
                realizarEnvio();
            }
        }.start());

        btnQrCode.setOnClickListener(v -> {
            boolean permitido = Permissao.validaPermissao(1,this, new String[] { Manifest.permission.CAMERA });

            if (permitido) {
                Intent intent = new Intent(this, BarCodeActivity.class);
                startActivityForResult(intent, TipoRetornoIntent.BARCODE_SCAN.ordinal());
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
                dialog.setTitle("Permissão Câmera");
                dialog.setMessage("É necessário aceitar o uso da câmera para utilizar esta funcionalidade.");
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        edtPesquisa.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateFilteredData();
                //Collections.sort(produtosFiltro);
            }
        });

        btnFileChooser.setOnClickListener(v -> showFileChooser());
    }

    private void showFileChooser() {
        boolean permitido = Permissao.validaPermissao(1,this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE});

        if (permitido) {
            new MaterialFilePicker()
                    .withActivity(MovimentacaoActivity.this)
                    .withRequestCode(TipoRetornoIntent.FILE_SEARCH.ordinal())
                    .withFilter(Pattern.compile(".*\\.txt$")) // Filtering files and directories by file name using regexp
                    //.withFilterDirectories(true) // Set directories filterable (false by default)
                    //.withHiddenFiles(true) // Show hidden files and folders
                    .start();
        } else {
            Util.AlertaInfo(MovimentacaoActivity.this, "Permissão arquivos", "É necessária permitir o acesso aos diretórios e arquivos do dispositivo.");
        }
    }

    private void updateFilteredData() {
        produtosFiltro.clear();

        for (Produto produto: produtos.values()) {
            if (matchesFilter(produto)) {
                produtosFiltro.add(produto);
            }
        }
    }

    private boolean matchesFilter(Produto produto) {
        String filterString = edtPesquisa.getText().toString().trim();

        if (filterString.isEmpty()) {
            return true;
        }

        String lowerCaseFilterString = filterString.toLowerCase();
        if (produto.getDescricao().toLowerCase().contains(lowerCaseFilterString)) {
            return true;
        }
        return produto.getCodReferencia().toLowerCase().contains(lowerCaseFilterString);

    }

    private void realizarEnvio() {
        handler.post(() -> inicializaProgressDialog("REGISTRANDO", "Registrando movimentação..."));

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
        } else { //
            handler.post(() -> inicializaProgressDialog("Consultando produto", "Consultando..."));

            Query query = databaseReference.child("Produto").orderByChild("codReferencia").startAt(codReferencia).endAt(codReferencia).limitToFirst(1);

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    boolean encontrou = false;

                    if (!dataSnapshot.exists()) {
                        handler.post(() -> {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }

                            Util.AlertaInfo(MovimentacaoActivity.this, "PRODUTO NÃO ENCONTRADO", "Produto " + edtCodReferencia.getText().toString() + " não encontrado.");
                            limparCampos();
                        });
                    } else {
                        String key = Objects.requireNonNull(dataSnapshot.getValue()).toString().split("=")[0].replaceAll("[^\\d]", "");

                        int qtde = 1;
                        int qtdeBruta = Integer.parseInt(edtQtde.getText().toString().isEmpty() ? "0" : edtQtde.getText().toString());
                        if (qtdeBruta > 1) qtde = qtdeBruta;

                        try {
                            encontrou = true;

                            boolean isAvariado = !tgbEstadoMercadoria.isChecked();
                            Movimentacao movimentacao = new Movimentacao(Calendar.getInstance(), isAvariado, qtde);
                            databaseReference.child("Produto").child(key).child("movimentacoes").child(UUID.randomUUID().toString()).setValue(movimentacao);

                            Toast.makeText(getApplicationContext(), "Movimentação cadastrada com sucesso.", Toast.LENGTH_LONG).show();
                            limparCampos();

                            handler.post(() -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
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

                        if (!encontrou) {
                            handler.post(() -> {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }

                                Util.AlertaInfo(MovimentacaoActivity.this, "PRODUTO NÃO ENCONTRADO", "Produto " + edtCodReferencia.getText().toString() + " não encontrado.");

                                limparCampos();
                            });
                        }

                        query.removeEventListener(this);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    handler.post(() -> handler.post(() -> {
                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                        Util.AlertaInfo(MovimentacaoActivity.this, "ERRO", "Houve algum erro ao cadastrar a movimentação no produto.");
                    }));
                }
            };

            query.addValueEventListener(valueEventListener);
        }
    }

    private void eventoDatabase() {
        databaseReference.child("Produto")
                         .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                produtos.clear();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshotProduto : dataSnapshot.getChildren()) {
                        produtos.put(snapshotProduto.getKey(), snapshotProduto.getValue(Produto.class));
                    }
                }

                updateFilteredData();
                //Collections.sort(produtosFiltro);

                //adapterProdutos = new MovimentacaoAdapter(produtosFiltro);
                //rcvInventario.setAdapter(adapterProdutos);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

        });
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (firebaseAuth.getCurrentUser() != null) {
            firebaseAuth.signOut();
        }

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}