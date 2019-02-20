package com.maxys.maxysinventory.model;

import java.util.ArrayList;
import java.util.List;

public class EmpresaContribuidor {

    private String idEmpresa;
    private List<String> idContribuidores;

    public EmpresaContribuidor() {
        idContribuidores = new ArrayList<>();
    }

    public EmpresaContribuidor(String idEmpresa, List<String> idContribuidores) {
        this.idEmpresa = idEmpresa;
        this.idContribuidores = idContribuidores;
    }

    public String getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(String idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public List<String> getIdContribuidores() {
        return idContribuidores;
    }

    public void setIdContribuidores(List<String> idContribuidores) {
        this.idContribuidores = idContribuidores;
    }

}