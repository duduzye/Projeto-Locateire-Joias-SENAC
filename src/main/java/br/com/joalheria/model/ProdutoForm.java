package br.com.joalheria.model;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProdutoForm {
    @NotBlank @Size(max=30) public String sku;
    @NotBlank @Size(max=100) public String nome;
    @Size(max=255) public String descricao;
    @NotNull @DecimalMin(value="0.01") public BigDecimal preco;
    @Size(max=20) public String tamanho;
    @DecimalMin(value="0.01") public BigDecimal peso;
    @NotNull public Integer categoria;
    @NotNull public Integer material;
    public Integer pedra;
    public boolean ativo = true;
    public String getSku(){return sku;}
    public void setSku(String value){sku=value;}
    public String getNome(){return nome;}
    public void setNome(String value){nome=value;}
    public String getDescricao(){return descricao;}
    public void setDescricao(String value){descricao=value;}
    public BigDecimal getPreco(){return preco;}
    public void setPreco(BigDecimal value){preco=value;}
    public String getTamanho(){return tamanho;}
    public void setTamanho(String value){tamanho=value;}
    public BigDecimal getPeso(){return peso;}
    public void setPeso(BigDecimal value){peso=value;}
    public Integer getCategoria(){return categoria;}
    public void setCategoria(Integer value){categoria=value;}
    public Integer getMaterial(){return material;}
    public void setMaterial(Integer value){material=value;}
    public Integer getPedra(){return pedra;}
    public void setPedra(Integer value){pedra=value;}
    public boolean isAtivo(){return ativo;}
    public void setAtivo(boolean value){ativo=value;}
}
