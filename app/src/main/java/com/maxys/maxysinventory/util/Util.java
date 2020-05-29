package com.maxys.maxysinventory.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.LogAcoes;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Util {

    public static final Locale BRAZIL_LOCALE = new Locale("pt", "br");

    private static final SimpleDateFormat formatadorData = new SimpleDateFormat("dd/MM/yyyy", BRAZIL_LOCALE);
    private static final SimpleDateFormat formatadorHoraMinuto = new SimpleDateFormat("HH:mm", BRAZIL_LOCALE);
    private static final SimpleDateFormat formatadorHoraCompleta = new SimpleDateFormat("HH:mm:ss", BRAZIL_LOCALE);

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

    public static AlertDialog.Builder AlertaInfo(Context context, String title, String message) {
        return AlertaInfo(context, title, message, null);
    }

    public static AlertDialog.Builder AlertaInfo(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                //.setView()
                //.setIcon()
                .setPositiveButton("OK", listener != null ? listener : (dialog, which) -> dialog.dismiss());
        builder.show();

        return builder;
    }

    public static AlertDialog.Builder Alerta(Context context, String title, String message) {
        return Alerta(context, title, message, null);
    }

    public static AlertDialog.Builder Alerta(Context context, String title, String message, DialogInterface.OnClickListener listener) {
        return new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                //.setView()
                //.setIcon()
                .setPositiveButton("OK", listener != null ? listener : (dialog, which) -> dialog.dismiss());
    }

    public static List<String> readFile(String path) {
        List<String> linhas = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String linha;
            while ((linha = bufferedReader.readLine()) != null) {
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

    public static String insereNCaracteres(String texto, String caracter, int tamanhoString, boolean esquerda) {
        String retorno = "";

        // Quantidade 0 (zero) preserva a string passada.
        if (tamanhoString == 0) {
            retorno = texto;
        } else {
            if (texto.length() < tamanhoString) {
                int qtde = tamanhoString - texto.length();
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
                retorno = texto.substring(0, tamanhoString);
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

    public static int converteInteiro(float numero) {
        return (int) numero;
    }

    public static int converteInteiro(double numero) {
        return (int) numero;
    }

    public static int converteInteiro(String numero) {
        return Integer.parseInt(numero);
    }

    public static String removerZerosEsquerda(String texto) {
        String retorno = texto;

        while (retorno.substring(0, 1).equals("0")) {
            retorno = retorno.substring(1);
        }

        return retorno;
    }

    public static String converteData(Date date) {
        return formatadorData.format(date);
    }

    public static String converteHoraMinuto(Date date) {
        return formatadorHoraMinuto.format(date);
    }

    public static String converteHoraCompleta(Date date) {
        return formatadorHoraCompleta.format(date);
    }

}
