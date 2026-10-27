package br.com.joalheria.controller;

import br.com.joalheria.model.FornecedorForm;
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
@RequestMapping("/fornecedores")
public class FornecedorController {
    private final JoalheriaRepository repo;
    public FornecedorController(JoalheriaRepository repo){this.repo=repo;}
    @GetMapping public String listar(Model model){model.addAttribute("fornecedores",repo.fornecedores());return "fornecedores";}
    @GetMapping("/novo") public String novo(Model model){model.addAttribute("fornecedorForm",new FornecedorForm());return "fornecedor-form";}
    @GetMapping("/{id}/editar") public String editar(@PathVariable int id,Model model){
        Map<String,Object> c=repo.fornecedor(id);if(c==null)return "redirect:/fornecedores";
        FornecedorForm f=new FornecedorForm();f.nome=(String)c.get("nome");f.cnpj=(String)c.get("cnpj");f.telefone=(String)c.get("telefone");f.email=(String)c.get("email");f.endereco=(String)c.get("endereco");
        f.ativo=c.get("ativo") instanceof Boolean b ? b : ((Number)c.get("ativo")).intValue()==1;
        model.addAttribute("fornecedorForm",f);model.addAttribute("editando",id);return "fornecedor-form";
    }
    @PostMapping("/salvar") public String salvar(@Valid @ModelAttribute FornecedorForm fornecedorForm,BindingResult valid,
                                                 @RequestParam(required=false) Integer id,Model model,RedirectAttributes flash){
        if(!valid.hasFieldErrors("cnpj")&&!cnpjValido(fornecedorForm.cnpj))valid.rejectValue("cnpj","cnpj.invalido","CNPJ inválido");
        if(valid.hasErrors()){model.addAttribute("editando",id);return "fornecedor-form";}
        try{
            if(id==null){repo.inserirFornecedor(fornecedorForm);flash.addFlashAttribute("sucesso","Fornecedor cadastrado!");}
            else{repo.atualizarFornecedor(id,fornecedorForm);flash.addFlashAttribute("sucesso","Fornecedor atualizado!");}
            return "redirect:/fornecedores";
        }catch(Exception e){model.addAttribute("erro",Mensagens.erro(e));model.addAttribute("editando",id);return "fornecedor-form";}
    }
    @PostMapping("/{id}/desativar") public String desativar(@PathVariable int id,RedirectAttributes flash){
        try{repo.desativarFornecedor(id);flash.addFlashAttribute("sucesso","Fornecedor desativado sem excluir o histórico.");}
        catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));}
        return "redirect:/fornecedores";
    }
    private static boolean cnpjValido(String c){
        if(c==null||!c.matches("\\d{14}")||c.chars().distinct().count()==1)return false;
        int[] p1={5,4,3,2,9,8,7,6,5,4,3,2},p2={6,5,4,3,2,9,8,7,6,5,4,3,2};
        return dv(c,p1)==c.charAt(12)-'0'&&dv(c,p2)==c.charAt(13)-'0';
    }
    private static int dv(String c,int[] p){int s=0;for(int i=0;i<p.length;i++)s+=(c.charAt(i)-'0')*p[i];int r=s%11;return r<2?0:11-r;}
}