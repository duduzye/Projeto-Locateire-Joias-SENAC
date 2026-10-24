
package br.com.joalheria.controller;

import br.com.joalheria.repository.JoalheriaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
public class InicioController {

    private final JoalheriaRepository repo;

    public InicioController(JoalheriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String inicio() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String inicio,
            @RequestParam(required = false) String fim,
            Model model) {

        LocalDate dataInicio = (inicio == null || inicio.isBlank())
                ? LocalDate.now().withDayOfMonth(1)
                : LocalDate.parse(inicio);

        LocalDate dataFim = (fim == null || fim.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(fim);

        if (dataInicio.isAfter(dataFim)) {
            dataInicio = dataFim;
        }

        // FILTROS DE DATA
        model.addAttribute("inicio", dataInicio);
        model.addAttribute("fim", dataFim);

        // INDICADORES GERAIS
        model.addAttribute("indicadores",
                repo.dashboard(dataInicio, dataFim));

        // VENDAS DO PERÍODO
        model.addAttribute("vendas",
                repo.dashboardDetalhado(dataInicio, dataFim));

        // RANKING DOS PRODUTOS MAIS VENDIDOS
        model.addAttribute("maisVendidos",
                repo.produtosMaisVendidos(dataInicio, dataFim));

        // NOVA FUNCIONALIDADE DO PI:
        // ANÁLISE INTELIGENTE DE ESTOQUE
        model.addAttribute("analiseEstoque",
                repo.analiseEstoque(dataInicio, dataFim));

        return "dashboard";
    }
}
