package br.com.joalheria.controller;

import br.com.joalheria.model.ProdutoForm;
import br.com.joalheria.repository.JoalheriaRepository;
import br.com.joalheria.service.Mensagens;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequestMapping("/produtos")
public class ProdutoController {
    private final JoalheriaRepository repo;
    public ProdutoController(JoalheriaRepository repo){this.repo=repo;}
    @GetMapping public String listar(Model model){model.addAttribute("produtos",repo.produtos());return "produtos";}
    private void opcoes(Model model){
        model.addAttribute("categorias",repo.categorias());model.addAttribute("materiais",repo.materiais());model.addAttribute("pedras",repo.pedras());
    }
    @GetMapping("/novo") public String novo(Model model){model.addAttribute("produtoForm",new ProdutoForm());model.addAttribute("editando",null);opcoes(model);return "produto-form";}
    @GetMapping("/{id}/editar") public String editar(@PathVariable int id,Model model){
        Map<String,Object> p=repo.produto(id);if(p==null)return "redirect:/produtos";
        ProdutoForm f=new ProdutoForm();f.sku=(String)p.get("sku");f.nome=(String)p.get("nome");f.descricao=(String)p.get("descricao");
        f.preco=(BigDecimal)p.get("preco");f.tamanho=(String)p.get("tamanho");f.peso=(BigDecimal)p.get("peso");
        f.categoria=(Integer)p.get("categoria");f.material=(Integer)p.get("material");f.pedra=(Integer)p.get("pedra");
        f.ativo=p.get("ativo") instanceof Boolean b ? b : ((Number)p.get("ativo")).intValue()==1;
        model.addAttribute("produtoForm",f);model.addAttribute("editando",id);opcoes(model);return "produto-form";
    }
    @PostMapping("/salvar") public String salvar(@Valid @ModelAttribute ProdutoForm produtoForm, BindingResult valid,
            @RequestParam(required=false) Integer id,Model model,RedirectAttributes flash){
        if(valid.hasErrors()){model.addAttribute("editando",id);opcoes(model);return "produto-form";}
        try{
            if(id==null){repo.inserirProduto(produtoForm);flash.addFlashAttribute("sucesso","Produto cadastrado!");}
            else{repo.atualizarProduto(id,produtoForm);flash.addFlashAttribute("sucesso","Produto atualizado!");}
            return "redirect:/produtos";
        }catch(Exception e){model.addAttribute("erro",Mensagens.erro(e));model.addAttribute("editando",id);opcoes(model);return "produto-form";}
    }
    @PostMapping("/{id}/desativar") public String desativar(@PathVariable int id,RedirectAttributes flash){
        try{repo.desativarProduto(id);flash.addFlashAttribute("sucesso","Produto desativado sem apagar o histórico.");}
        catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));}
        return "redirect:/produtos";
    }
}
