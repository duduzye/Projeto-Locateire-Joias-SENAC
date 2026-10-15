package br.com.joalheria.controller;

import br.com.joalheria.model.ClienteForm;
import br.com.joalheria.repository.JoalheriaRepository;
import br.com.joalheria.service.Mensagens;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;

@Controller
@RequestMapping("/clientes")
public class ClienteController {
    private final JoalheriaRepository repo;
    public ClienteController(JoalheriaRepository repo){this.repo=repo;}
    @GetMapping public String listar(Model model){model.addAttribute("clientes",repo.clientes());return "clientes";}
    @GetMapping("/novo") public String novo(Model model){model.addAttribute("clienteForm",new ClienteForm());return "cliente-form";}
    @GetMapping("/{id}/editar") public String editar(@PathVariable int id,Model model){
        Map<String,Object> c=repo.cliente(id);if(c==null)return "redirect:/clientes";
        ClienteForm f=new ClienteForm();f.nome=(String)c.get("nome");f.cpf=(String)c.get("cpf");f.telefone=(String)c.get("telefone");f.email=(String)c.get("email");
        f.ativo=c.get("ativo") instanceof Boolean b ? b : ((Number)c.get("ativo")).intValue()==1;
        model.addAttribute("clienteForm",f);model.addAttribute("editando",id);return "cliente-form";
    }
    @PostMapping("/salvar") public String salvar(@Valid @ModelAttribute ClienteForm clienteForm,BindingResult valid,
                 @RequestParam(required=false) Integer id,Model model,RedirectAttributes flash){
        if(valid.hasErrors()){model.addAttribute("editando",id);return "cliente-form";}
        try{
            if(id==null){repo.inserirCliente(clienteForm);flash.addFlashAttribute("sucesso","Cliente cadastrado!");}
            else{repo.atualizarCliente(id,clienteForm);flash.addFlashAttribute("sucesso","Cliente atualizado!");}
            return "redirect:/clientes";
        }catch(Exception e){model.addAttribute("erro",Mensagens.erro(e));model.addAttribute("editando",id);return "cliente-form";}
    }
    @PostMapping("/{id}/desativar") public String desativar(@PathVariable int id,RedirectAttributes flash){
        try{repo.desativarCliente(id);flash.addFlashAttribute("sucesso","Cliente desativado sem excluir o histórico.");}
        catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));}
        return "redirect:/clientes";
    }
}
