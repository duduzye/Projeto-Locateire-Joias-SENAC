package br.com.joalheria.service;

public final class Mensagens {
    private Mensagens() { }
    public static String erro(Exception ex) {
        String m = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
        if (m.contains("duplicate entry")) return "Este cadastro já existe (SKU, CPF, login ou número repetido).";
        if (m.contains("foreign key constraint")) return "O registro está vinculado a outros dados.";
        if (m.contains("estoque insuficiente")) return "Estoque insuficiente para concluir a operação.";
        if (m.contains("produto inexistente") || m.contains("inativo")) return "Produto não encontrado ou inativo.";
        if (m.contains("senha atual")) return "A senha atual está incorreta.";
        if (m.contains("data truncation") || m.contains("out of range") || m.contains("check constraint")) return "Revise os valores informados.";
        if (ex instanceof IllegalArgumentException) return ex.getMessage();
        return "Não foi possível concluir a operação. Confira os campos e a conexão com o banco.";
    }
}
