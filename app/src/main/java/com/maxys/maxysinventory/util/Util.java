package com.maxys.maxysinventory.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.LogAcoes;
import com.maxys.maxysinventory.model.Movimentacao;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

public class Util {

    public static ProgressDialog inicializaProgressDialog(Context context, String title, String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);

        progressDialog.setIcon(R.drawable.ic_launcher_foreground);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        progressDialog.setTitle(title);
        progressDialog.setMessage(message);

        return progressDialog;
    }

    public static void finalizarProgressDialog(Handler handler, ProgressDialog progressDialog) {
        if (progressDialog.isShowing()) {
            handler.post(progressDialog::dismiss);
        }
    }

    public static AlertDialog AlertaInfo(Context context, String title, String message) {
        return AlertaInfo(context, title, message, null);
    }

    public static AlertDialog AlertaInfo(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(context).setTitle(title).setMessage(message).
                setPositiveButton("OK", listener != null ? listener : (dialog, which) -> dialog.dismiss()).show();
        //alert.setView();
        //alert.setIcon();
    }

    public static List<String> readFile (String path){
        List<String> linhas = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String linha;
            while((linha = bufferedReader.readLine()) != null){
                linhas.add(linha);
            }
            fileReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return linhas;
    }

    public static void salvarLog(String idEmpresa, String idUsuarioLogado, String descricao) {
        LogAcoes logAcoes = new LogAcoes();
        logAcoes.setIdEmpresa(idEmpresa);
        logAcoes.setIdUsuario(idUsuarioLogado);
        logAcoes.setDescricao(descricao);
        logAcoes.salvarLog();

        logAcoes = null;
    }

    public static String insereNCaracteres(String texto, String caracter, int qtdeCaracteres, boolean esquerda) {
        String retorno = "";

        // Quantidade 0 (zero) preserva a string passada.
        if (qtdeCaracteres == 0) {
            retorno = texto;
        } else {
            if (texto.length() < qtdeCaracteres) {
                int qtde = qtdeCaracteres - texto.length();
                StringBuilder caracterPreenchimento = new StringBuilder();
                for (int i = 0; i < qtde; i++) {
                    caracterPreenchimento.insert(0, caracter);
                }

                if (esquerda) {
                    retorno = caracterPreenchimento + texto;
                } else {
                    retorno = texto + caracterPreenchimento;
                }
            } else {
                retorno = texto.substring(0, qtdeCaracteres);
            }
        }

        return retorno;
    }

    public static String insereZeros(double numero, int parteInteira, int parteFracionaria) {
        String retorno;

        String texto = String.valueOf(numero).replaceAll(",", "");
        if (texto.contains(".")) {
            String[] partes = texto.split("\\.");
            partes[0] = insereNCaracteres(partes[0], "0", parteInteira, true);
            partes[1] = insereNCaracteres(partes[1], "0", parteFracionaria, false);

            retorno = partes[0] + "." + partes[1];
        } else {
            String esquerda = insereNCaracteres(texto, "0", parteInteira, true);
            String direita = insereNCaracteres(texto, "0", parteFracionaria, false);

            retorno = esquerda + "." + direita;
        }

        return retorno;
    }

}
