package br.com.joalheria.controller;

import br.com.joalheria.model.ItemPedido;
import br.com.joalheria.repository.JoalheriaRepository;
import br.com.joalheria.service.Mensagens;
import br.com.joalheria.service.VendaService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
public class VendaController {
    private final JoalheriaRepository repo;
    private final VendaService service;
    public VendaController(JoalheriaRepository repo, VendaService service){this.repo=repo;this.service=service;}
    @GetMapping("/vendas") public String listar(Model m){m.addAttribute("vendas",repo.vendas());return "vendas";}
    @GetMapping("/vendas/nova") public String nova(Model m){m.addAttribute("clientes",repo.clientesAtivos());m.addAttribute("produtos",repo.produtosAtivos());return "venda-form";}
    @PostMapping("/vendas/salvar") public String salvar(@RequestParam int cliente,
            @RequestParam(name="produto") List<Integer> produtos,
            @RequestParam(name="quantidade") List<Integer> quantidades,
            Authentication auth,RedirectAttributes flash){
        try{
            if(produtos.size()!=quantidades.size())throw new IllegalArgumentException("Confira os itens da venda.");
            List<ItemPedido> itens=new ArrayList<>();
            for(int i=0;i<produtos.size();i++)itens.add(new ItemPedido(produtos.get(i),quantidades.get(i)));
            int venda=service.realizarVenda(cliente,auth.getName(),itens);
            flash.addFlashAttribute("sucesso","Venda #"+venda+" concluída e nota acadêmica gerada!");
            return "redirect:/notas/"+venda;
        }catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));return "redirect:/vendas/nova";}
    }
    @GetMapping("/notas/{venda}") public String nota(@PathVariable int venda,Model m){
        var nota=repo.nota(venda);if(nota==null){m.addAttribute("erro","Esta venda ainda não possui nota fiscal.");return "nota-inexistente";}
        m.addAttribute("nota",nota);m.addAttribute("itens",repo.itensNota(venda));return "nota";
    }
}
