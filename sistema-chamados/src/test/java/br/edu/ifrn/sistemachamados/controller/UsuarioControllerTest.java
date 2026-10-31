package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class)
class UsuarioControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean UsuarioService usuarioService;

    @Test
    void criaSolicitanteETecnicoComContratoDeValidacao() throws Exception {
        when(usuarioService.cadastrar(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(3L);
            return usuario;
        });
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"matricula\":\"20260010\",\"tipo\":\"SOLICITANTE\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.tipo").value("SOLICITANTE"))
                .andExpect(jsonPath("$.setorId").doesNotExist());
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Técnico\",\"email\":\"tecnico@example.com\",\"matricula\":\"20260011\",\"tipo\":\"TECNICO\",\"setorId\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.setorId").value(1));
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@example.com\",\"matricula\":\"1\",\"tipo\":\"SOLICITANTE\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"invalido\",\"matricula\":\"1\",\"tipo\":\"SOLICITANTE\"}"))
                .andExpect(status().isBadRequest());
        when(usuarioService.cadastrar(any(Usuario.class))).thenThrow(new DadosInvalidosException("Técnico deve possuir setor."));
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Técnico\",\"email\":\"t@example.com\",\"matricula\":\"1\",\"tipo\":\"TECNICO\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"matricula\":\"1\",\"tipo\":\"OUTRO\"}"))
                .andExpect(status().isBadRequest());
        when(usuarioService.cadastrar(any(Usuario.class))).thenThrow(new RegraNegocioException("Já existe um usuário com este e-mail."));
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"matricula\":\"1\",\"tipo\":\"SOLICITANTE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void listaUsuariosComFiltrosPaginacaoEOrdenacao() throws Exception {
        when(usuarioService.listar(eq("Ana"), eq(TipoUsuario.SOLICITANTE), eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ControllerTestData.usuario(3, TipoUsuario.SOLICITANTE, null))));
        mvc.perform(get("/usuarios").param("nome", "Ana").param("tipo", "SOLICITANTE")
                        .param("setorId", "1").param("sort", "nome,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].email").value("usuario3@example.com"))
                .andExpect(jsonPath("$.totalElements").value(1));
        verify(usuarioService).listar(eq("Ana"), eq(TipoUsuario.SOLICITANTE), eq(1L),
                argThat(p -> p.getSort().getOrderFor("nome").isDescending()));
        mvc.perform(get("/usuarios").param("page", "-1")).andExpect(status().isBadRequest());
        mvc.perform(get("/usuarios").param("size", "0")).andExpect(status().isBadRequest());
        mvc.perform(get("/usuarios").param("size", "51")).andExpect(status().isBadRequest());
        mvc.perform(get("/usuarios").param("sort", "inexistente,asc")).andExpect(status().isBadRequest());
    }

    @Test
    void consultaAtualizaEListaChamadosDoUsuario() throws Exception {
        Usuario solicitante = ControllerTestData.usuario(3, TipoUsuario.SOLICITANTE, null);
        Chamado chamado = ControllerTestData.chamado(8);
        when(usuarioService.buscarPorId(3L)).thenReturn(solicitante);
        when(usuarioService.atualizar(eq(3L), any(Usuario.class))).thenReturn(solicitante);
        when(usuarioService.listarChamados(eq(3L), eq(StatusChamado.ABERTO), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(chamado)));
        mvc.perform(get("/usuarios/3")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(3));
        mvc.perform(put("/usuarios/3").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Ana\",\"email\":\"ana@example.com\",\"matricula\":\"20260010\",\"tipo\":\"SOLICITANTE\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/usuarios/3/chamados").param("status", "ABERTO").param("sort", "dataAbertura,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(8));
        verify(usuarioService).listarChamados(eq(3L), eq(StatusChamado.ABERTO),
                argThat(p -> p.getSort().getOrderFor("dataAbertura").isDescending()));
    }
}
