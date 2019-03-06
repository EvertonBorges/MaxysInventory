package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Empresa;
import com.maxys.maxysinventory.model.TipoRetornoIntent;
import com.maxys.maxysinventory.util.Permissao;
import com.maxys.maxysinventory.util.Preferencias;
import com.maxys.maxysinventory.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

        Preferencias preferencias = new Preferencias(ProdutoMenuActivity.this);
        String nome = preferencias.getNome();
        edtNomeUsuario.setText(nome);

        Intent it = getIntent();
        empresa = (Empresa) it.getSerializableExtra("empresa");

        String data = "Data: " + formatadorData.format(Calendar.getInstance().getTime());
        edtDataHora.setText(data);

        btnImportar.setOnClickListener(v -> showFileChooser());

        btnExportar.setOnClickListener(v -> {
            //Intent it = new Intent(ProdutoMenuActivity.this, );
            //it.putExtra("contribuidor", contribuidor);

            //startActivity(it);
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TipoRetornoIntent.FILE_SEARCH.ordinal()) {
            if (resultCode == RESULT_OK) {
                List<String> docPaths = new ArrayList<>();
                docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                for (String path: docPaths) {
                    Util.AlertaInfo(ProdutoMenuActivity.this, "DADOS ARQUIVO", "Arquivo: " + path + "\nDados:\n" + Util.readFile(path).toString());
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