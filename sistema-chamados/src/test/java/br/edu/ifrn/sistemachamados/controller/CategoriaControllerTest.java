package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.service.CategoriaService;
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

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CategoriaService categoriaService;

    @Test
    void criaCategoriaEValidaNomeESetor() throws Exception {
        when(categoriaService.cadastrar(any(Categoria.class))).thenReturn(ControllerTestData.categoria(2, 1));
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Rede\",\"descricao\":\"Internet\",\"setorId\":1}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.setorId").value(1));
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"setorId\":1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Rede\"}"))
                .andExpect(status().isBadRequest());
        when(categoriaService.cadastrar(any(Categoria.class))).thenThrow(new RecursoNaoEncontradoException("Setor não encontrado."));
        mvc.perform(post("/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Rede\",\"setorId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listaFiltraPorNomeESetorEPagina() throws Exception {
        when(categoriaService.listar(eq("red"), eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ControllerTestData.categoria(2, 1))));
        mvc.perform(get("/categorias").param("nome", "red").param("setorId", "1").param("page", "0").param("size", "50"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].nome").value("Rede"))
                .andExpect(jsonPath("$.page").value(0));
        verify(categoriaService).listar(eq("red"), eq(1L), argThat(p -> p.getPageSize() == 50));
    }

    @Test
    void consultaAtualizaEExcluiCategoria() throws Exception {
        when(categoriaService.buscarPorId(2L)).thenReturn(ControllerTestData.categoria(2, 1));
        when(categoriaService.atualizar(eq(2L), any(Categoria.class))).thenReturn(ControllerTestData.categoria(2, 1));
        mvc.perform(get("/categorias/2")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(2));
        mvc.perform(put("/categorias/2").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Rede\",\"setorId\":1}"))
                .andExpect(status().isOk());
        mvc.perform(delete("/categorias/2")).andExpect(status().isNoContent());
        doThrow(new RegraNegocioException("Categoria possui chamados.")).when(categoriaService).excluir(3L);
        mvc.perform(delete("/categorias/3")).andExpect(status().isConflict());
    }
}
