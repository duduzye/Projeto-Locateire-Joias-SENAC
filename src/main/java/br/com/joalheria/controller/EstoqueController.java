package br.com.joalheria.controller;

import br.com.joalheria.repository.JoalheriaRepository;
import br.com.joalheria.service.Mensagens;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {
    private final JoalheriaRepository repo;
    public EstoqueController(JoalheriaRepository repo){this.repo=repo;}
    @GetMapping public String listar(Model model){
        model.addAttribute("produtos",repo.produtosAtivos());
        model.addAttribute("fornecedores",repo.fornecedoresAtivos());
        model.addAttribute("movimentos",repo.movimentacoes());return "estoque";
    }
    @PostMapping("/movimentar") public String movimentar(@RequestParam int produto,@RequestParam String tipo,
                                                         @RequestParam int quantidade,@RequestParam(required=false) String observacao,
                                                         @RequestParam(required=false) Integer fornecedor,
                                                         Authentication authentication, RedirectAttributes flash){
        try{
            if(quantidade<=0)throw new IllegalArgumentException("A quantidade precisa ser maior que zero.");
            if("ENTRADA".equals(tipo)&&fornecedor==null)throw new IllegalArgumentException("Selecione o fornecedor para registrar uma entrada.");
            int usuario=repo.donaId(authentication.getName());
            if("ENTRADA".equals(tipo))repo.entradaEstoque(produto,usuario,quantidade,observacao,fornecedor);
            else if("SAIDA".equals(tipo))repo.saidaEstoque(produto,usuario,quantidade,observacao);
            else throw new IllegalArgumentException("Tipo de movimentação inválido.");
            flash.addFlashAttribute("sucesso","Estoque atualizado!");
        }catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));}
        return "redirect:/estoque";
    }
}
