package br.com.joalheria.Pck_Controller;

import br.com.joalheria.Pck_Model.ContaForm;
import br.com.joalheria.Pck_Persistencia.UsuarioPersistencia;
import br.com.joalheria.Service.Mensagens;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.stream.Collectors;

@Controller
@RequestMapping("/conta")
public class ContaController {

    private final UsuarioPersistencia repo;

    public ContaController(UsuarioPersistencia repo) {
        this.repo = repo;
    }

    @GetMapping
    public String conta(Authentication auth, Model m) {
        m.addAttribute("login", auth.getName());
        return "conta";
    }

    @PostMapping("/senha")
    public String senha(
            @Valid @ModelAttribute ContaForm contaForm,
            BindingResult valid,
            Authentication auth,
            RedirectAttributes flash) {

        // Regras da senha nova (tamanho, maiúscula, caractere especial e
        // confirmação) ficam no ContaForm. A senha nunca volta para a tela.
        if (valid.hasErrors()) {
            flash.addFlashAttribute("erro", mensagem(valid));
            return "redirect:/conta";
        }

        try {

            // Alterar senha no banco de dados
            repo.trocarSenha(
                    repo.donaId(auth.getName()),
                    contaForm.senhaAtual,
                    contaForm.senhaNova
            );

            flash.addFlashAttribute(
                    "sucesso",
                    "Senha alterada! Use a senha nova no próximo login."
            );

        } catch (Exception e) {
            flash.addFlashAttribute("erro", Mensagens.erro(e));
        }

        return "redirect:/conta";
    }

    // Junta todas as regras que falharam, para a usuária corrigir de uma vez.
    private String mensagem(BindingResult valid) {
        return valid.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .distinct()
                .collect(Collectors.joining(" "));
    }
}