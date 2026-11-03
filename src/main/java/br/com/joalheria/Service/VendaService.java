package br.com.joalheria.Service;

import br.com.joalheria.Pck_Model.ItemPedido;
import br.com.joalheria.Pck_Persistencia.UsuarioPersistencia;
import br.com.joalheria.Pck_Persistencia.VendaPersistencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendaService {
    private final VendaPersistencia vendas;
    private final UsuarioPersistencia usuarios;

    public VendaService(VendaPersistencia vendas, UsuarioPersistencia usuarios){
        this.vendas=vendas;
        this.usuarios=usuarios;
    }

    @Transactional
    public int realizarVenda(int cliente,String login,List<ItemPedido> itens){
        if(itens==null||itens.isEmpty())throw new IllegalArgumentException("Adicione ao menos um produto à venda.");
        Integer usuario=usuarios.donaId(login);
        if(!vendas.clienteAtivo(cliente))throw new IllegalArgumentException("Selecione um cliente ativo.");
        // Mesma transação + bloqueio de cada produto: evita duas vendas reduzirem simultaneamente um estoque antigo.
        for(ItemPedido item:itens){
            if(item.produto()==null||item.quantidade()==null||item.quantidade()<=0)
                throw new IllegalArgumentException("Informe produto e quantidade positiva.");
            if(vendas.saldoBloqueado(item.produto())==null)throw new IllegalArgumentException("Produto não encontrado ou inativo.");
        }
        int venda=vendas.inserirVenda(cliente,usuario);
        for(ItemPedido item:itens){
            Integer saldo=vendas.saldoBloqueado(item.produto());
            if(saldo==null)throw new IllegalArgumentException("Produto não encontrado ou inativo.");
            if(saldo<item.quantidade())throw new IllegalArgumentException("Estoque insuficiente para a quantidade escolhida.");
            // O trigger original faz o desconto e calcula o total da venda.
            vendas.inserirItem(venda,item.produto(),item.quantidade());
            // Histórico de SAIDA da venda; não chamar PROC_SAIDA_ESTOQUE, pois descontaria duas vezes.
            vendas.registrarSaidaEstoque(item.produto(),usuario,item.quantidade(),venda);
        }
        vendas.inserirNotaFiscal(venda);
        return venda;
    }
}