package br.com.joalheria.model;

import jakarta.validation.constraints.*;

public class FornecedorForm {
    @NotBlank @Size(max=150) public String nome;
    @NotBlank @Pattern(regexp="^[0-9]{14}$", message="O CNPJ deve ter 14 dígitos") public String cnpj;
    @Pattern(regexp="^$|^[0-9]{10,11}$", message="Use 10 ou 11 dígitos ou deixe em branco") public String telefone;
    @Size(max=100) @Email public String email;
    @Size(max=255) public String endereco;
    public boolean ativo = true;
    public String getNome(){return nome;}
    public void setNome(String v){nome=v;}
    public String getCnpj(){return cnpj;}
    public void setCnpj(String v){cnpj=v==null?null:v.replaceAll("\\D","");} // aceita com máscara
    public String getTelefone(){return telefone;}
    public void setTelefone(String v){telefone=v;}
    public String getEmail(){return email;}
    public void setEmail(String v){email=v;}
    public String getEndereco(){return endereco;}
    public void setEndereco(String v){endereco=v;}
    public boolean isAtivo(){return ativo;}
    public void setAtivo(boolean v){ativo=v;}
}