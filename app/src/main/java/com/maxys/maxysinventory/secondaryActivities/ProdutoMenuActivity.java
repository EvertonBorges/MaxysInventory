package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.FragmentActivity;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import droidninja.filepicker.models.sort.SortingTypes;
import droidninja.filepicker.utils.Orientation;

public class ProdutoMenuActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    private final SimpleDateFormat formatadorData = new SimpleDateFormat("dd/MM/yyyy");

    private Empresa empresa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_produto_menu);

        TextView edtNomeUsuario = findViewById(R.id.txt_menu_produto_usuario);
        TextView edtDataHora = findViewById(R.id.txt_menu_produto_data);
        Button btnImportar = findViewById(R.id.btn_menu_produto_importar);
        Button btnExportar = findViewById(R.id.btn_menu_produto_exportar);
        Button btnGerenciar = findViewById(R.id.btn_menu_produto_gerenciar);

        PreferenciasStatic preferencias = PreferenciasStatic.getInstance();
        Contribuidor contribuidor = preferencias.getContribuidor();
        List<String> permissoes = new ArrayList<>();
        for (com.maxys.maxysinventory.model.Permissao permissao: contribuidor.getPermissoes()) {
            if (!permissoes.contains(permissao.getNome())) {
                permissoes.add(permissao.getNome());
            }
        }

        boolean permitirImportarProdutos = permissoes.contains("actImportarProdutos");
        boolean permitirExportarProdutos = permissoes.contains("actExportarProdutos");

        edtNomeUsuario.setText(contribuidor.getNome());
        btnImportar.setVisibility(permitirImportarProdutos ? View.VISIBLE : View.GONE);
        btnExportar.setVisibility(permitirExportarProdutos ? View.VISIBLE : View.GONE);

        Intent it = getIntent();
        empresa = (Empresa) it.getSerializableExtra("empresa");

        String data = "Data: " + formatadorData.format(Calendar.getInstance().getTime());
        edtDataHora.setText(data);

        btnImportar.setOnClickListener(v -> showFileChooser());

        btnExportar.setOnClickListener(v -> exportarProdutos());

        btnGerenciar.setOnClickListener(v -> {
            Intent intent = new Intent(ProdutoMenuActivity.this, ManageProdutoActivity.class);
            intent.putExtra("empresa", empresa);
            startActivity(intent);
        });

    }

    private void showFileChooser() {
        boolean permitido = Permissao.validaPermissao(1,this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE});

        if (permitido) {
             FilePickerBuilder.Companion.getInstance()
                                        .setMaxCount(1)
                                        .setActivityTheme(R.style.LibAppTheme)
                                        .setActivityTitle("Selecione o arquivo")
                                        .addFileSupport("TXT", new String[] {".txt"}, R.drawable.ic_insert_drive_file)
                                        .enableDocSupport(false)
                                        .enableSelectAll(false)
                                        .sortDocumentsBy(SortingTypes.name)
                                        .withOrientation(Orientation.PORTRAIT_ONLY)
                                        .pickFile(this, TipoRetornoIntent.FILE_SEARCH.ordinal());
        } else {
            Util.AlertaInfo(ProdutoMenuActivity.this, "Permissão arquivos", "É necessária permitir o acesso aos diretórios e arquivos do dispositivo.");
        }
    }

    private void exportarProdutos() {
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
                    perquisarProdutos(fileExt);
                } else {
                    Util.AlertaInfo(ProdutoMenuActivity.this, "EXPORTAR ARQUIVO", "Falha ao criar o arquivo.");
                }
            } else {
                perquisarProdutos(fileExt);
            }
        } catch (IOException ex) {
            Util.AlertaInfo(ProdutoMenuActivity.this, "EXPORTAR ARQUIVO", "Falha ao exportar os produtos.");
        }
    }

    private void exportaArquivo(File fileExt, String conteudo) throws IOException {
        //Abre o arquivo
        FileOutputStream fosExt = new FileOutputStream(fileExt);

        //Escreve no arquivo
        fosExt.write(conteudo.getBytes());

        //Obrigatoriamente você precisa fechar
        fosExt.close();

        Util.AlertaInfo(ProdutoMenuActivity.this,"EXPORTAR ARQUIVO","Arquivo exportado com sucesso.");
    }

    private void perquisarProdutos(File fileExt) {
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

                             for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
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

                                                      if (movimentacao.isAvariado()) {
                                                          inventario.addAvarias(movimentacao.getQtde());
                                                      } else {
                                                          inventario.addSaldo(movimentacao.getQtde());
                                                      }

                                                      handler.post(() -> progressDialog.incrementProgressBy(1));
                                                  }

                                                  StringBuilder conteudoArquivo = new StringBuilder();
                                                  int i = 0;
                                                  for (Inventario inventario: inventarios.values()) {
                                                      String codReferencia = Util.insereNCaracteres(inventario.getCodReferencia(), "0", 13, true);
                                                      String saldo = Util.insereZeros(inventario.getSaldo(), 7, 5).replaceAll("\\.", "");
                                                      String avariados = Util.insereZeros(inventario.getAvariados(), 7, 5).replaceAll("\\.", "");

                                                      String linha = codReferencia + saldo + avariados;

                                                      conteudoArquivo.append(linha);
                                                      i++;

                                                      if (i < inventarios.size()) {
                                                          conteudoArquivo.append("\n");
                                                      }
                                                  }

                                                  try {
                                                      exportaArquivo(fileExt, conteudoArquivo.toString());
                                                      Util.finalizarProgressDialog(handler, progressDialog);
                                                  } catch (IOException ex) {
                                                      Util.finalizarProgressDialog(handler, progressDialog);

                                                      Util.AlertaInfo(ProdutoMenuActivity.this,
                                                              "EXPORTAR ARQUIVO",
                                                              "Falha ao exportar o arquivo.");
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
                List<String> docPaths = new ArrayList<>(Objects.requireNonNull(data).getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                for (String path: docPaths) {
                    List<String> linhas = Util.readFile(path);

                    AlertDialog.Builder alert = new AlertDialog.Builder(ProdutoMenuActivity.this);
                    alert.setTitle("DESEJA INSERIR OS DADOS?");
                    alert.setMessage("Deseja realmente inserir os seguintes dados?\n" +
                                     "Caminho: " + path + "\n" +
                                     "Dados:\n" + linhas.toString());
                    alert.setCancelable(true);
                    alert.setPositiveButton("Sim", (dialog, which) -> {
                        DatabaseReference reference = ConfiguracaoFirebase.getFirebase();

                        StringBuilder builder = new StringBuilder();
                        final Integer[] qtdeErros = {0};

                        for (String linha: linhas) {
                            Produto produto = new Produto();
                            produto.setCodReferencia(linha.substring(0, 13));
                            produto.setDescricao(linha.substring(13));

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
                                        } else if (linha.equals(linhas.get(linhas.size() - 1))) { // última linha
                                            if (qtdeErros[0] > 0) {
                                                Util.AlertaInfo(ProdutoMenuActivity.this, "ERROS:\n\n", builder.toString());
                                            }
                                        }
                                     });
                        }
                    });
                    alert.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
                    alert.show();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onOpenFragmentClicked(View view) {
        Intent intent = new Intent(this, FragmentActivity.class);
        startActivity(intent);
    }

}