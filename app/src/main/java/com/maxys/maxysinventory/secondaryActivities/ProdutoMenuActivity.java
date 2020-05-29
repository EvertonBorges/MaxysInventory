package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;
import com.maxys.maxysinventory.model.Contribuidor;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.Inventario;
import com.maxys.maxysinventory.model.Movimentacao;
import com.maxys.maxysinventory.model.Produto;
import com.maxys.maxysinventory.model.TipoRetornoIntent;
import com.maxys.maxysinventory.util.Permissao;
import com.maxys.maxysinventory.util.PreferenciasStatic;
import com.maxys.maxysinventory.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import droidninja.filepicker.utils.Orientation;

public class ProdutoMenuActivity extends AppCompatActivity {

    private Empresa empresa;

    public static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_menu);

        Button btnImportar = findViewById(R.id.btn_menu_produto_importar);
        Button btnExportar = findViewById(R.id.btn_menu_produto_exportar);
        Button btnGerenciar = findViewById(R.id.btn_menu_produto_gerenciar);

        Toolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setTitle(this.getString(R.string.txtManageProdutos));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        Contribuidor contribuidor = preferencias.getContribuidor();
        List<String> permissoes = new ArrayList<>();
        for (com.maxys.maxysinventory.model.Permissao permissao : contribuidor.getPermissoes()) {
            if (!permissoes.contains(permissao.getNome())) {
                permissoes.add(permissao.getNome());
            }
        }

        boolean permitirImportarProdutos = permissoes.contains("actImportarProdutos");
        boolean permitirExportarProdutos = permissoes.contains("actExportarProdutos");

        btnImportar.setVisibility(permitirImportarProdutos ? View.VISIBLE : View.GONE);
        btnExportar.setVisibility(permitirExportarProdutos ? View.VISIBLE : View.GONE);

        Intent it = getIntent();
        empresa = (Empresa) it.getSerializableExtra("empresa");

        btnImportar.setOnClickListener(v -> showFileChooser());

        btnExportar.setOnClickListener(v -> exportar());

        btnGerenciar.setOnClickListener(v -> {
            Intent intent = new Intent(ProdutoMenuActivity.this, ManageProdutoActivity.class);
            intent.putExtra("empresa", empresa);
            startActivity(intent);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.item_sair);
        item.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFileChooser() {
        boolean permitido = Permissao.validaPermissao(1, this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE});

        if (permitido) {
            FilePickerBuilder.Companion.getInstance()
                    .setMaxCount(1)
                    .setActivityTheme(R.style.LibAppTheme)
                    .setActivityTitle("Selecione o arquivo")
                    .addFileSupport("TXT", new String[]{".txt"}, R.drawable.ic_insert_drive_file)
                    .enableDocSupport(false)
                    .enableSelectAll(false)
                    .sortDocumentsBy(SortingTypes.name)
                    .withOrientation(Orientation.PORTRAIT_ONLY)
                    .pickFile(this, TipoRetornoIntent.FILE_SEARCH.ordinal());
        } else {
            Util.AlertaInfo(ProdutoMenuActivity.this, "Permissão arquivos", "É necessária permitir o acesso aos diretórios e arquivos do dispositivo.");
        }
    }

    private void exportar() {
        String nomeDiretorio = "Maxys Inventory";
        String diretorioApp = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + nomeDiretorio + "/";
        File diretorio = new File(diretorioApp);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        // Quando o File() tem um parâmetro ele cria um diretório.
        // Quando tem dois ele cria um arquivo no diretório onde é informado.
        File fileExt = new File(diretorioApp, "Produtos.txt");

        try {
            if (!fileExt.exists()) {
                // Cria o arquivo
                if (fileExt.createNewFile()) {
                    exportarProdutos(fileExt);
                } else {
                    Util.AlertaInfo(ProdutoMenuActivity.this, "EXPORTAR ARQUIVO", "Falha ao criar o arquivo.");
                }
            } else {
                exportarProdutos(fileExt);
            }
        } catch (IOException ex) {
            Util.AlertaInfo(ProdutoMenuActivity.this, "EXPORTAR ARQUIVO", "Falha ao exportarProdutos os produtos.");
        }
    }

    private void exportaArquivo(File fileExt, String conteudo) throws IOException {
        //Abre o arquivo
        FileOutputStream fosExt = new FileOutputStream(fileExt);

        //Escreve no arquivo
        fosExt.write(conteudo.getBytes());

        //Obrigatoriamente você precisa fechar
        fosExt.close();

        Util.AlertaInfo(ProdutoMenuActivity.this, "EXPORTAR ARQUIVO", "Arquivo exportado com sucesso.");
    }

    private void exportarProdutos(File fileExt) {
        Handler handler = new Handler();

        ProgressDialog progressDialog = Util.inicializaProgressDialog(ProdutoMenuActivity.this, "EXPORTAR", "Gerando arquivo...");

        handler.post(progressDialog::show);

        DatabaseReference reference = ConfiguracaoFirebase.getFirebase();
        reference.child("empresa_produtos")
                .child(empresa.getId())
                .orderByChild("codReferencia")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            HashMap<String, Inventario> inventarios = new HashMap<>();

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                Produto produto = snapshot.getValue(Produto.class);

                                Inventario inventario = new Inventario();
                                inventario.setCodReferencia(produto.getCodReferencia());
                                inventario.setDescricao(produto.getDescricao());

                                inventarios.put(snapshot.getKey(), inventario);
                            }

                            reference.child("empresa_movimentacoes")
                                    .child(empresa.getId())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue() != null) {
                                                handler.post(() -> {
                                                    progressDialog.setTitle("GERANDO ARQUIVO");
                                                    progressDialog.setMessage("Gerando arquivo, por favor aguarde um momento.");
                                                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                                    progressDialog.setProgress(0);
                                                    progressDialog.setMax((int) dataSnapshot.getChildrenCount());
                                                });

                                                for (DataSnapshot snapshot1 : dataSnapshot.getChildren()) {
                                                    Movimentacao movimentacao = snapshot1.getValue(Movimentacao.class);

                                                    Inventario inventario = inventarios.get(movimentacao.getIdProduto());
                                                    inventario.setDataHoraMovimentacao(movimentacao.getDataHoraMovimentacao());

                                                    if (movimentacao.isAvariado()) {
                                                        inventario.addAvarias(movimentacao.getQtde());
                                                    } else {
                                                        inventario.addSaldo(movimentacao.getQtde());
                                                    }
                                                }

                                                StringBuilder conteudoArquivo = new StringBuilder();
                                                int i = 0;
                                                for (Inventario inventario : inventarios.values()) {
                                                    String codReferencia = "";
                                                    String codInterno = "";

                                                    if (inventario.getCodReferencia().length() < 12) {
                                                        codInterno = Util.insereNCaracteres(inventario.getCodReferencia(), "0", 6, true);
                                                    } else {
                                                        codReferencia = inventario.getCodReferencia();
                                                    }

                                                    Date date = new Date(inventario.getDataHoraMovimentacao());
                                                    String data = Util.converteData(date);
                                                    String hora = Util.converteHoraMinuto(date);
                                                    String saldo = String.valueOf(Util.converteInteiro(inventario.getSaldo()));

                                                    //String codReferencia = Util.insereNCaracteres(inventario.getCodReferencia(), "0", 13, true);
                                                    //String avariados = Util.insereZeros(inventario.getAvariados(), 7, 5).replaceAll("\\.", "");

                                                    String linha =
                                                            data + ',' +
                                                                    hora + ',' +
                                                                    codReferencia + ',' +
                                                                    (saldo.equals("0") ? "" : saldo) + ',' +
                                                                    codInterno;

                                                    conteudoArquivo.append(linha);
                                                    i++;

                                                    if (i < inventarios.size()) {
                                                        conteudoArquivo.append("\n");
                                                    }

                                                    handler.post(() -> progressDialog.incrementProgressBy(1));
                                                }

                                                try {
                                                    exportaArquivo(fileExt, conteudoArquivo.toString());
                                                    Util.finalizarProgressDialog(handler, progressDialog);
                                                } catch (IOException ex) {
                                                    Util.finalizarProgressDialog(handler, progressDialog);

                                                    Util.AlertaInfo(ProdutoMenuActivity.this,
                                                            "EXPORTAR ARQUIVO",
                                                            "Falha ao exportarProdutos o arquivo.");
                                                }
                                            } else {
                                                Util.finalizarProgressDialog(handler, progressDialog);

                                                Util.AlertaInfo(ProdutoMenuActivity.this,
                                                        "EXPORTAR ARQUIVO",
                                                        "Não há movimentações registradas, por isso não foi gerado arquivo.");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        } else {
                            Util.finalizarProgressDialog(handler, progressDialog);

                            Util.AlertaInfo(ProdutoMenuActivity.this,
                                    "EXPORTAR ARQUIVO",
                                    "Não há produtos registrados, por isso não foi gerado arquivo.");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TipoRetornoIntent.FILE_SEARCH.ordinal()) {
            if (resultCode == RESULT_OK) {
                final ArrayList<String> filesToImport =
                        Objects.requireNonNull(data)
                                .getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS);

                if (filesToImport != null && !filesToImport.isEmpty()) {
                    for (String path : filesToImport) {
                        List<String> linhas = Util.readFile(path);

                        AlertDialog.Builder alert = new AlertDialog.Builder(ProdutoMenuActivity.this);
                        alert.setTitle("DESEJA INSERIR OS DADOS?");
                        alert.setMessage(
                                "Deseja realmente inserir os seguintes dados?\n" +
                                "Caminho: " + path + "\n" +
                                "Dados:\n" + linhas.toString()
                        );
                        alert.setCancelable(true);
                        alert.setPositiveButton("Sim", (dialog, which) -> {
                            DatabaseReference reference = ConfiguracaoFirebase.getFirebase();

                            StringBuilder builder = new StringBuilder();
                            final Integer[] qtdeErros = {0};

                            Handler handler = new Handler();

                            handler.post(() -> inicializaProgressDialog("IMPORTAÇÃO", "Importando produtos..."));

                            for (String linha : linhas) {
                                Produto produto = new Produto();
                                String[] line = linha.split(";");
                                if (line.length >= 2) {
                                    produto.setCodReferencia(line[0].trim());
                                    produto.setDescricao(line[1].trim());
                                } else if (line.length == 1) {
                                    produto.setCodReferencia(line[0].trim());
                                }

                                reference.child("empresa_produtos")
                                        .child(empresa.getId())
                                        .push()
                                        .setValue(produto)
                                        .addOnCompleteListener(task -> {
                                            if (!task.isSuccessful()) {
                                                qtdeErros[0]++;
                                                String erro = "Erro " + qtdeErros[0] + ": " +
                                                        "\tCód. Ref.: " + produto.getCodReferencia() + "\n" +
                                                        "\tDescrição: " + produto.getDescricao();

                                                builder.append(erro);
                                            }

                                            if (linha.equals(linhas.get(linhas.size() - 1))) { // última linha
                                                handler.post(() -> {
                                                    if (progressDialog.isShowing()) {
                                                        progressDialog.dismiss();
                                                    }

                                                    Util.AlertaInfo(ProdutoMenuActivity.this, "IMPORTAÇÃO", (qtdeErros[0] == 0 ? "Produtos importados com sucesso." : "Erro ao importas or produtos:\n\n" + builder.toString()));
                                                });
                                            }
                                        });
                            }
                        });
                        alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                        alert.show();
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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

}