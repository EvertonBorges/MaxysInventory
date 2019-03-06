package com.maxys.maxysinventory.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.maxys.maxysinventory.config.ConfiguracaoFirebase;

import java.util.Calendar;

public class LogAcoes {

    private String idEmpresa;
    private String idUsuario;
    private Long dataHoraAcao;
    private String descricao;

    public LogAcoes() {
        this.idEmpresa = null;
        this.idUsuario = null;
        this.descricao = null;

        setDataHoraAcao(Calendar.getInstance().getTimeInMillis());
    }

    public void salvarLog() {
        DatabaseReference databaseReference = ConfiguracaoFirebase.getFirebase()
                                                                  .child("log_acoes");

        databaseReference.push().setValue(this);
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getDataHoraAcao() {
        return dataHoraAcao;
    }

    public void setDataHoraAcao(Long dataHoraAcao) {
        this.dataHoraAcao = dataHoraAcao;
    }

    @Exclude
    public Calendar getDataHora() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.dataHoraAcao);
        return calendar;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}