package br.com.joalheria.Pck_Model;

import jakarta.validation.constraints.*;

public class EstoqueForm {
    @NotNull(message="Selecione o produto.") public Integer produto;
    @NotBlank(message="Selecione o tipo da movimentação.")
    @Pattern(regexp="^(ENTRADA|SAIDA)$", message="Tipo de movimentação inválido.") public String tipo;
    @NotNull(message="Informe a quantidade.")
    @Min(value=1, message="A quantidade precisa ser maior que zero.") public Integer quantidade;
    @Size(max=255, message="A observação pode ter no máximo 255 caracteres.") public String observacao;
    public Integer fornecedor;

    // Entrada exige fornecedor; saída ignora o campo.
    @AssertTrue(message="Selecione o fornecedor para registrar uma entrada.")
    public boolean isFornecedorValido(){
        return !"ENTRADA".equals(tipo) || fornecedor!=null;
    }

    public Integer getProduto(){return produto;}
    public void setProduto(Integer v){produto=v;}
    public String getTipo(){return tipo;}
    public void setTipo(String v){tipo=v;}
    public Integer getQuantidade(){return quantidade;}
    public void setQuantidade(Integer v){quantidade=v;}
    public String getObservacao(){return observacao;}
    public void setObservacao(String v){observacao=v;}
    public Integer getFornecedor(){return fornecedor;}
    public void setFornecedor(Integer v){fornecedor=v;}
}