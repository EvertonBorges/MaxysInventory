package com.maxys.maxysinventory.util;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;

import com.maxys.maxysinventory.R;
import com.maxys.maxysinventory.model.LogAcoes;
import com.maxys.maxysinventory.model.Movimentacao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
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

    public static Calendar maiorCalendar(Collection<Movimentacao> movimentacoes) {
        List<Calendar> calendars = new ArrayList<>();
        if (movimentacoes != null) {
            if (!movimentacoes.isEmpty()) {
                for (Movimentacao movimentacao: movimentacoes) {
                    calendars.add(movimentacao.getDataHora());
                }
            }
        }
        return maiorCalendar(calendars);
    }

    public static Calendar maiorCalendar(List<Calendar> calendars) {
        Calendar retorno = null;
        if (calendars != null) {
            if (!calendars.isEmpty()) {
                for (Calendar calendar: calendars) {
                    if (calendar.compareTo(retorno) > 0) {
                        retorno = calendar;
                    }
                }
            }
        }
        return retorno;
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

}
