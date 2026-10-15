package br.com.joalheria.model;

import jakarta.validation.constraints.*;

public class ClienteForm {
    @NotBlank @Size(max=100) public String nome;
    @Pattern(regexp="^$|^[0-9]{11}$", message="Use 11 dígitos ou deixe em branco") public String cpf;
    @Pattern(regexp="^$|^[0-9]{10,11}$", message="Use 10 ou 11 dígitos ou deixe em branco") public String telefone;
    @Size(max=100) @Email public String email;
    public boolean ativo = true;
    public String getNome(){return nome;}
    public void setNome(String v){nome=v;}
    public String getCpf(){return cpf;}
    public void setCpf(String v){cpf=v;}
    public String getTelefone(){return telefone;}
    public void setTelefone(String v){telefone=v;}
    public String getEmail(){return email;}
    public void setEmail(String v){email=v;}
    public boolean isAtivo(){return ativo;}
    public void setAtivo(boolean v){ativo=v;}
}
