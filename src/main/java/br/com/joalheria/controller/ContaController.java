
package br.com.joalheria.controller;

import br.com.joalheria.repository.JoalheriaRepository;
import br.com.joalheria.service.Mensagens;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/conta")
public class ContaController {

    private final JoalheriaRepository repo;

    public ContaController(JoalheriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public String conta(Authentication auth, Model m) {
        m.addAttribute("login", auth.getName());
        return "conta";
    }

    @PostMapping("/senha")
    public String senha(
            @RequestParam String senhaAtual,
            @RequestParam String senhaNova,
            @RequestParam String confirmacao,
            Authentication auth,
            RedirectAttributes flash) {

        try {

            // Mínimo de 8 caracteres
            if (senhaNova.length() < 8) {
                throw new IllegalArgumentException(
                        "A nova senha precisa ter pelo menos 8 caracteres."
                );
            }

            // Pelo menos uma letra maiúscula
            if (!senhaNova.matches(".*[A-Z].*")) {
                throw new IllegalArgumentException(
                        "A nova senha precisa ter pelo menos uma letra maiúscula."
                );
            }

            // Pelo menos um caractere especial
            if (!senhaNova.matches("(?s).*[^\\p{L}\\p{N}\\s].*")) {
                throw new IllegalArgumentException(
                        "A nova senha precisa ter pelo menos um caractere especial, como @, #, ! ou $."
                );
            }

            // Confirmar senha
            if (!senhaNova.equals(confirmacao)) {
                throw new IllegalArgumentException(
                        "A confirmação não coincide com a senha nova."
                );
            }

            // Alterar senha no banco de dados
            repo.trocarSenha(
                    repo.donaId(auth.getName()),
                    senhaAtual,
                    senhaNova
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
}
