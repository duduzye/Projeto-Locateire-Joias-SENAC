package br.com.joalheria.Pck_Model;

import jakarta.validation.constraints.*;

public class ContaForm {
    @NotBlank(message="Informe a senha atual.") public String senhaAtual;

    @NotNull(message="Informe a nova senha.")
    @Size(min=8, message="A nova senha precisa ter pelo menos 8 caracteres.")
    @Pattern(regexp="(?s).*[A-Z].*", message="A nova senha precisa ter pelo menos uma letra maiúscula.")
    @Pattern(regexp="(?s).*[^\\p{L}\\p{N}\\s].*", message="A nova senha precisa ter pelo menos um caractere especial, como @, #, ! ou $.")
    public String senhaNova;

    public String confirmacao;

    @AssertTrue(message="A confirmação não coincide com a senha nova.")
    public boolean isConfirmacaoValida(){
        return senhaNova==null || senhaNova.equals(confirmacao);
    }

    public String getSenhaAtual(){return senhaAtual;}
    public void setSenhaAtual(String v){senhaAtual=v;}
    public String getSenhaNova(){return senhaNova;}
    public void setSenhaNova(String v){senhaNova=v;}
    public String getConfirmacao(){return confirmacao;}
    public void setConfirmacao(String v){confirmacao=v;}
}