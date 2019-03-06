package com.maxys.maxysinventory.config;

public class Nomes {

    private static String CHAVE_USUARIO = "usuarios";
    private static String CHAVE_EMPRESA = "empresas";
    private static String CHAVE_PRODUTO = "produtos";

    public static String getChaveUsuario() {
        return CHAVE_USUARIO;
    }

    public static String getChaveEmpresa() {
        return CHAVE_EMPRESA;
    }

    public static String getChaveProduto() {
        return CHAVE_PRODUTO;
    }

}