package br.com.joalheria.Pck_Controller;

import br.com.joalheria.Pck_Model.VendaForm;
import br.com.joalheria.Pck_Persistencia.ClientePersistencia;
import br.com.joalheria.Pck_Persistencia.ProdutoPersistencia;
import br.com.joalheria.Pck_Persistencia.VendaPersistencia;
import br.com.joalheria.Service.Mensagens;
import br.com.joalheria.Service.VendaService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VendaController {
    private final VendaPersistencia vendaPersistencia;
    private final ClientePersistencia clientePersistencia;
    private final ProdutoPersistencia produtoPersistencia;
    private final VendaService service;
    public VendaController(VendaPersistencia vendaPersistencia, ClientePersistencia clientePersistencia,
                           ProdutoPersistencia produtoPersistencia, VendaService service){
        this.vendaPersistencia=vendaPersistencia;
        this.clientePersistencia=clientePersistencia;
        this.produtoPersistencia=produtoPersistencia;
        this.service=service;
    }
    @GetMapping("/vendas") public String listar(Model m){m.addAttribute("vendas",vendaPersistencia.vendas());return "vendas";}
    @GetMapping("/vendas/nova") public String nova(Model m){m.addAttribute("clientes",clientePersistencia.clientesAtivos());m.addAttribute("produtos",produtoPersistencia.produtosAtivos());return "venda-form";}
    @PostMapping("/vendas/salvar") public String salvar(@Valid @ModelAttribute VendaForm vendaForm,BindingResult valid,
                                                        Authentication auth,RedirectAttributes flash){
        if(valid.hasErrors()){flash.addFlashAttribute("erro",mensagem(valid));return "redirect:/vendas/nova";}
        try{
            int venda=service.realizarVenda(vendaForm.cliente,auth.getName(),vendaForm.itens());
            flash.addFlashAttribute("sucesso","Venda #"+venda+" concluída e nota acadêmica gerada!");
            return "redirect:/notas/"+venda;
        }catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));return "redirect:/vendas/nova";}
    }
    @GetMapping("/notas/{venda}") public String nota(@PathVariable int venda,Model m){
        var nota=vendaPersistencia.nota(venda);if(nota==null){m.addAttribute("erro","Esta venda ainda não possui nota fiscal.");return "nota-inexistente";}
        m.addAttribute("nota",nota);m.addAttribute("itens",vendaPersistencia.itensNota(venda));return "nota";
    }
    // Valor não numérico vira mensagem amigável; nos demais casos usa o texto da validação.
    private String mensagem(BindingResult valid){
        boolean conversao=valid.getAllErrors().stream().anyMatch(e->e instanceof FieldError f&&f.isBindingFailure());
        return conversao?"Confira os itens da venda.":valid.getAllErrors().get(0).getDefaultMessage();
    }
}