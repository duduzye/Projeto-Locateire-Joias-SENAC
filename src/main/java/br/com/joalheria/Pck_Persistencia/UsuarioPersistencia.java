package br.com.joalheria.Pck_Persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UsuarioPersistencia {

    private final JdbcTemplate jdbc;

    public UsuarioPersistencia(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // AUTORIZACAO

    public Integer donaId(String login) {
        List<Integer> ids = jdbc.queryForList(
                "SELECT A01_codigo FROM USUARIO_01 " +
                        "WHERE A01_login=? AND A01_ativo=TRUE " +
                        "AND A01_perfil='DONA'",
                Integer.class,
                login
        );

        if (ids.isEmpty()) {
            throw new IllegalArgumentException("Usuária não autorizada.");
        }

        return ids.get(0);
    }

    // SENHA

    public void trocarSenha(
            int usuario, String antiga, String nova) {

        jdbc.update(
                "CALL PROC_TROCAR_SENHA_DONA(?,?,?)",
                usuario,
                antiga,
                nova
        );
    }
}