package br.com.joalheria.Pck_Controller;

import br.com.joalheria.Pck_Model.EstoqueForm;
import br.com.joalheria.Pck_Persistencia.ProdutoPersistencia;
import br.com.joalheria.Pck_Persistencia.EstoquePersistencia;
import br.com.joalheria.Pck_Persistencia.FornecedorPersistencia;
import br.com.joalheria.Pck_Persistencia.UsuarioPersistencia;
import br.com.joalheria.Service.Mensagens;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {
    private final ProdutoPersistencia produtos;
    private final FornecedorPersistencia fornecedores;
    private final EstoquePersistencia estoque;
    private final UsuarioPersistencia usuarios;
    public EstoqueController(ProdutoPersistencia produtos, FornecedorPersistencia fornecedores,
                             EstoquePersistencia estoque, UsuarioPersistencia usuarios){
        this.produtos=produtos;
        this.fornecedores=fornecedores;
        this.estoque=estoque;
        this.usuarios=usuarios;
    }
    @GetMapping public String listar(Model model){
        model.addAttribute("produtos",produtos.produtosAtivos());
        model.addAttribute("fornecedores",fornecedores.fornecedoresAtivos());
        model.addAttribute("movimentos",estoque.movimentacoes());return "estoque";
    }
    @PostMapping("/movimentar") public String movimentar(@Valid @ModelAttribute EstoqueForm estoqueForm,BindingResult valid,
                                                         Authentication authentication, RedirectAttributes flash){
        if(valid.hasErrors()){flash.addFlashAttribute("erro",mensagem(valid));return "redirect:/estoque";}
        try{
            int usuario=usuarios.donaId(authentication.getName());
            if("ENTRADA".equals(estoqueForm.tipo))estoque.entradaEstoque(estoqueForm.produto,usuario,estoqueForm.quantidade,estoqueForm.observacao,estoqueForm.fornecedor);
            else if("SAIDA".equals(estoqueForm.tipo))estoque.saidaEstoque(estoqueForm.produto,usuario,estoqueForm.quantidade,estoqueForm.observacao);
            else throw new IllegalArgumentException("Tipo de movimentação inválido.");
            flash.addFlashAttribute("sucesso","Estoque atualizado!");
        }catch(Exception e){flash.addFlashAttribute("erro",Mensagens.erro(e));}
        return "redirect:/estoque";
    }
    // Valor não numérico vira mensagem amigável; nos demais casos usa o texto da validação.
    private String mensagem(BindingResult valid){
        boolean conversao=valid.getAllErrors().stream().anyMatch(e->e instanceof FieldError f&&f.isBindingFailure());
        return conversao?"Confira os dados da movimentação.":valid.getAllErrors().get(0).getDefaultMessage();
    }
}