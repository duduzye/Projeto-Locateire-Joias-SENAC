package br.com.joalheria.service;

import br.com.joalheria.model.ItemPedido;
import br.com.joalheria.repository.JoalheriaRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VendaService {
    private final JdbcTemplate jdbc;
    private final JoalheriaRepository repository;
    public VendaService(JdbcTemplate jdbc, JoalheriaRepository repository){this.jdbc=jdbc;this.repository=repository;}

    @Transactional
    public int realizarVenda(int cliente,String login,List<ItemPedido> itens){
        if(itens==null||itens.isEmpty())throw new IllegalArgumentException("Adicione ao menos um produto à venda.");
        Integer usuario=repository.donaId(login);
        Integer clienteAtivo=jdbc.queryForObject("SELECT COUNT(*) FROM CLIENTE_06 WHERE A06_codigo=? AND A06_ativo=TRUE",Integer.class,cliente);
        if(clienteAtivo==null||clienteAtivo==0)throw new IllegalArgumentException("Selecione um cliente ativo.");
        // Mesma transação + bloqueio de cada produto: evita duas vendas reduzirem simultaneamente um estoque antigo.
        for(ItemPedido item:itens){
            if(item.produto()==null||item.quantidade()==null||item.quantidade()<=0)
                throw new IllegalArgumentException("Informe produto e quantidade positiva.");
            List<Integer> saldo=jdbc.queryForList("SELECT A05_quantidade_estoque FROM PRODUTO_05 WHERE A05_codigo=? AND A05_ativo=TRUE FOR UPDATE",Integer.class,item.produto());
            if(saldo.isEmpty())throw new IllegalArgumentException("Produto não encontrado ou inativo.");
        }
        jdbc.update("CALL PROC_INS_VENDA(?,?)",cliente,usuario);
        Integer venda=jdbc.queryForObject("SELECT LAST_INSERT_ID()",Integer.class);
        if(venda==null||venda<=0)throw new IllegalStateException("Não foi possível identificar a venda.");
        for(ItemPedido item:itens){
            int saldo=jdbc.queryForObject("SELECT A05_quantidade_estoque FROM PRODUTO_05 WHERE A05_codigo=? AND A05_ativo=TRUE FOR UPDATE",Integer.class,item.produto());
            if(saldo<item.quantidade())throw new IllegalArgumentException("Estoque insuficiente para a quantidade escolhida.");
            // O trigger original faz o desconto e calcula o total da venda.
            jdbc.update("CALL PROC_INS_ITEM_VENDA(?,?,?)",venda,item.produto(),item.quantidade());
            // Histórico de SAIDA da venda; não chamar PROC_SAIDA_ESTOQUE, pois descontaria duas vezes.
            jdbc.update("""
              INSERT INTO MOVIMENTACAO_ESTOQUE_09(A09_tipo,A09_quantidade,A09_observacao,A05_codigo,A01_codigo)
              VALUES('SAIDA',?,?,?,?)
            """,item.quantidade(),"Venda #"+venda,item.produto(),usuario);
        }
        jdbc.update("CALL PROC_INS_NOTA_FISCAL(?)",venda);
        return venda;
    }
}
