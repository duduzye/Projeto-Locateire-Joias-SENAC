package br.com.joalheria.Pck_Model;

import jakarta.validation.constraints.*;

import java.util.ArrayList;
import java.util.List;

public class VendaForm {
    @NotNull(message="Selecione um cliente ativo.") public Integer cliente;
    @NotEmpty(message="Adicione ao menos um produto à venda.")
    public List<@NotNull(message="Selecione o produto de cada item.") Integer> produto = new ArrayList<>();
    @NotEmpty(message="Informe a quantidade dos produtos.")
    public List<@NotNull(message="Informe a quantidade de cada produto.")
    @Min(value=1, message="A quantidade deve ser maior que zero.") Integer> quantidade = new ArrayList<>();

    @AssertTrue(message="Confira os itens da venda.")
    public boolean isItensConsistentes(){
        return produto!=null && quantidade!=null && produto.size()==quantidade.size();
    }

    // Junta produto[i] com quantidade[i]. Usar somente depois da validação.
    public List<ItemPedido> itens(){
        List<ItemPedido> lista=new ArrayList<>();
        for(int i=0;i<produto.size();i++)lista.add(new ItemPedido(produto.get(i),quantidade.get(i)));
        return lista;
    }

    public Integer getCliente(){return cliente;}
    public void setCliente(Integer v){cliente=v;}
    public List<Integer> getProduto(){return produto;}
    public void setProduto(List<Integer> v){produto=v;}
    public List<Integer> getQuantidade(){return quantidade;}
    public void setQuantidade(List<Integer> v){quantidade=v;}
}