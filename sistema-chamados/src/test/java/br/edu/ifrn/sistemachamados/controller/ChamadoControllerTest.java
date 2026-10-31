package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Interacao;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.service.ChamadoService;
import br.edu.ifrn.sistemachamados.service.InteracaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChamadoController.class)
class ChamadoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean ChamadoService chamadoService;
    @MockitoBean InteracaoService interacaoService;

    @Test
    void abreChamadoERejeitaCorposInvalidos() throws Exception {
        Chamado chamado = ControllerTestData.chamado(10);
        when(chamadoService.abrirChamado(any(Chamado.class))).thenReturn(chamado);
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(chamadoJson()))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("ABERTO")).andExpect(jsonPath("$.prioridade").value("ALTA"));
        for (String corpo : List.of(
                "{\"titulo\":\"\",\"descricao\":\"x\",\"local\":\"Sala\",\"solicitanteId\":3,\"categoriaId\":2}",
                "{\"titulo\":\"x\",\"descricao\":\"\",\"local\":\"Sala\",\"solicitanteId\":3,\"categoriaId\":2}",
                "{\"titulo\":\"x\",\"descricao\":\"d\",\"local\":\"\",\"solicitanteId\":3,\"categoriaId\":2}",
                "{\"titulo\":\"x\",\"descricao\":\"d\",\"local\":\"Sala\",\"categoriaId\":2}",
                "{\"titulo\":\"x\",\"descricao\":\"d\",\"local\":\"Sala\",\"solicitanteId\":3}",
                "{\"titulo\":\"x\",\"descricao\":\"d\",\"local\":\"Sala\",\"prioridade\":\"URGENTE\",\"solicitanteId\":3,\"categoriaId\":2}")) {
            mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(corpo))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.timestamp").exists())
                    .andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.caminho").value("/chamados"));
        }
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content("{invalido"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/chamados")).andExpect(status().isBadRequest());
    }

    @Test
    void listaConverteTodosOsFiltrosDatasEPaginacao() throws Exception {
        when(chamadoService.listar(eq(StatusChamado.ABERTO), eq(Prioridade.ALTA), eq(1L), eq(2L), eq(3L), eq(4L),
                any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ControllerTestData.chamado(10)), org.springframework.data.domain.PageRequest.of(0, 10), 1));
        mvc.perform(get("/chamados").param("status", "ABERTO").param("prioridade", "ALTA").param("setorId", "1")
                        .param("categoriaId", "2").param("solicitanteId", "3").param("tecnicoId", "4")
                        .param("dataInicio", "2026-01-01").param("dataFim", "2026-01-31")
                        .param("sort", "dataAbertura,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(10));
        verify(chamadoService).listar(eq(StatusChamado.ABERTO), eq(Prioridade.ALTA), eq(1L), eq(2L), eq(3L), eq(4L),
                eq(LocalDateTime.of(2026, 1, 1, 0, 0)),
                eq(LocalDateTime.of(2026, 1, 31, 23, 59, 59, 999_999_999)),
                argThat(p -> p.getSort().getOrderFor("dataAbertura").isDescending()));
        mvc.perform(get("/chamados").param("dataInicio", "2026-02-01").param("dataFim", "2026-01-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/chamados").param("dataInicio", "data-invalida")).andExpect(status().isBadRequest());
        mvc.perform(get("/chamados").param("sort", "campoInexistente,asc")).andExpect(status().isBadRequest());
    }

    @Test
    void detalhaChamadoComInteracoesCronologicasENaoEncontrado() throws Exception {
        when(chamadoService.buscarPorId(10L)).thenReturn(ControllerTestData.chamado(10));
        Interacao primeira = ControllerTestData.interacao(1, 10, 3, 1);
        Interacao segunda = ControllerTestData.interacao(2, 10, 3, 2);
        when(interacaoService.listarPorChamado(eq(10L), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(primeira, segunda)));
        mvc.perform(get("/chamados/10")).andExpect(status().isOk()).andExpect(jsonPath("$.chamado.titulo").value("Sem internet"))
                .andExpect(jsonPath("$.interacoes[0].id").value(1)).andExpect(jsonPath("$.interacoes[1].id").value(2));
        when(chamadoService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Chamado não encontrado."));
        mvc.perform(get("/chamados/99")).andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void assumeResolveCancelaERegistraInteracaoNosContratosHttp() throws Exception {
        Chamado chamado = ControllerTestData.chamado(10);
        when(chamadoService.assumirChamado(10L, 4L)).thenReturn(chamado);
        when(chamadoService.resolverChamado(10L, 4L, "Ajustado")).thenReturn(chamado);
        when(chamadoService.cancelarChamado(10L, 3L)).thenReturn(chamado);
        when(interacaoService.registrarInteracao(10L, 3L, "Ainda ocorre"))
                .thenReturn(ControllerTestData.interacao(1, 10, 3, 1));
        mvc.perform(patch("/chamados/10/assumir").contentType(MediaType.APPLICATION_JSON).content("{\"tecnicoId\":4}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/chamados/10/resolver").contentType(MediaType.APPLICATION_JSON).content("{\"tecnicoId\":4,\"solucao\":\"Ajustado\"}"))
                .andExpect(status().isOk());
        mvc.perform(patch("/chamados/10/cancelar").contentType(MediaType.APPLICATION_JSON).content("{\"solicitanteId\":3}"))
                .andExpect(status().isOk());
        mvc.perform(post("/chamados/10/interacoes").contentType(MediaType.APPLICATION_JSON).content("{\"autorId\":3,\"texto\":\"Ainda ocorre\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.chamadoId").value(10));
        for (String url : List.of("/chamados/10/assumir", "/chamados/10/resolver", "/chamados/10/cancelar")) {
            mvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(patch("/chamados/10/resolver").contentType(MediaType.APPLICATION_JSON).content("{\"tecnicoId\":4,\"solucao\":\" \"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/chamados/10/interacoes").contentType(MediaType.APPLICATION_JSON).content("{\"texto\":\"x\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/chamados/10/interacoes").contentType(MediaType.APPLICATION_JSON).content("{\"autorId\":3,\"texto\":\" \"}"))
                .andExpect(status().isBadRequest());
        when(chamadoService.assumirChamado(11L, 4L)).thenThrow(new RegraNegocioException("Técnico não pertence ao setor responsável pelo chamado."));
        when(chamadoService.resolverChamado(11L, 4L, "Ajustado")).thenThrow(new RegraNegocioException("Somente o técnico responsável pode resolver o chamado."));
        when(chamadoService.cancelarChamado(11L, 3L)).thenThrow(new RegraNegocioException("Apenas chamados abertos podem ser cancelados."));
        when(interacaoService.registrarInteracao(11L, 3L, "x")).thenThrow(new RegraNegocioException("Não é possível registrar interações em chamado encerrado."));
        mvc.perform(patch("/chamados/11/assumir").contentType(MediaType.APPLICATION_JSON).content("{\"tecnicoId\":4}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/chamados/11/resolver").contentType(MediaType.APPLICATION_JSON).content("{\"tecnicoId\":4,\"solucao\":\"Ajustado\"}"))
                .andExpect(status().isConflict());
        mvc.perform(patch("/chamados/11/cancelar").contentType(MediaType.APPLICATION_JSON).content("{\"solicitanteId\":3}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/chamados/11/interacoes").contentType(MediaType.APPLICATION_JSON).content("{\"autorId\":3,\"texto\":\"x\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void padronizaConflitoDeIntegridadeEErroInesperadoSemDetalhesInternos() throws Exception {
        when(chamadoService.abrirChamado(any(Chamado.class))).thenThrow(new DataIntegrityViolationException("detalhe interno"));
        mvc.perform(post("/chamados").contentType(MediaType.APPLICATION_JSON).content(chamadoJson()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value("Conflito de integridade."))
                .andExpect(jsonPath("$.timestamp").exists()).andExpect(jsonPath("$.caminho").value("/chamados"));
        when(chamadoService.buscarPorId(500L)).thenThrow(new IllegalStateException("senha=segredo"));
        mvc.perform(get("/chamados/500")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro interno do servidor."))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("segredo"))));
    }

    private static String chamadoJson() {
        return "{\"titulo\":\"Sem internet\",\"descricao\":\"Internet indisponível\",\"local\":\"Sala 1\",\"prioridade\":\"ALTA\",\"solicitanteId\":3,\"categoriaId\":2}";
    }
}
