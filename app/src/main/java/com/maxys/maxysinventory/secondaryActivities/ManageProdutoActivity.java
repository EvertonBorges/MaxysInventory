package com.maxys.maxysinventory.secondaryActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.Usuario;
import com.maxys.maxysinventory.model.Produto;
import com.maxys.maxysinventory.model.TipoRetornoIntent;
import com.maxys.maxysinventory.util.Permissao;
import com.maxys.maxysinventory.util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ManageProdutoActivity extends AppCompatActivity {

    private Usuario contribuidor;
    private List<Produto> produtos;

    private EditText edtCodReferencia;
    private EditText edtDescricao;

    private String[] permissoesNecessarias = new String[] { Manifest.permission.INTERNET };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_produto);

        contribuidor = (Usuario) getIntent().getSerializableExtra("contribuidor");

        edtCodReferencia = findViewById(R.id.txt_manage_produto_cod_ref);
        edtDescricao = findViewById(R.id.txt_manage_produto_descricao);

        edtCodReferencia.setEnabled(false);
        edtDescricao.setEnabled(false);

        ImageButton btnBarcode = findViewById(R.id.btn_manage_produto_barcode);
        ImageButton btnLimpar = findViewById(R.id.btn_manage_produto_limpar);
        ImageButton btnSalvar = findViewById(R.id.btn_manage_produto_salvar);

        btnBarcode.setOnClickListener(v -> {
            boolean permitido = Permissao.validaPermissao(1,this, new String[] { Manifest.permission.CAMERA });

            if (permitido) {
                Intent intent = new Intent(ManageProdutoActivity.this, BarCodeActivity.class);
                startActivityForResult(intent, TipoRetornoIntent.BARCODE_SCAN.ordinal());
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
                dialog.setTitle("Permissão Câmera");
                dialog.setMessage("É necessário aceitar o uso da câmera para utilizar esta funcionalidade.");
                dialog.setCancelable(true);
                dialog.show();
            }
        });

        btnLimpar.setOnClickListener(v -> {

        });

        btnSalvar.setOnClickListener(v -> {


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
}
