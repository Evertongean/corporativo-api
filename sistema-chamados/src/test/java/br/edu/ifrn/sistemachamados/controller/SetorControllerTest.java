package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.service.CategoriaService;
import br.edu.ifrn.sistemachamados.service.SetorService;
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

@WebMvcTest(SetorController.class)
class SetorControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean SetorService setorService;
    @MockitoBean CategoriaService categoriaService;

    @Test
    void cadastraSetorERejeitaCamposObrigatoriosEDuplicidade() throws Exception {
        Setor setor = ControllerTestData.setor(1);
        when(setorService.cadastrar(any(Setor.class))).thenReturn(setor);
        mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Tecnologia\",\"sigla\":\"TI\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sigla").value("TI"));
        mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"\",\"sigla\":\"TI\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.caminho").value("/setores"));
        mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Tecnologia\",\"sigla\":\" \"}"))
                .andExpect(status().isBadRequest());
        when(setorService.cadastrar(any(Setor.class))).thenThrow(new RegraNegocioException("Sigla já existe."));
        mvc.perform(post("/setores").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Tecnologia\",\"sigla\":\"TI\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value("Sigla já existe."));
    }

    @Test
    void listaComFiltrosPaginacaoEOrdenacao() throws Exception {
        when(setorService.listar(eq("Tec"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ControllerTestData.setor(1)), org.springframework.data.domain.PageRequest.of(0, 10), 1));
        mvc.perform(get("/setores").param("nome", "Tec").param("sort", "nome,desc"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].nome").value("Tecnologia"))
                .andExpect(jsonPath("$.page").value(0)).andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.totalPages").value(1));
        verify(setorService).listar(eq("Tec"), argThat(p -> p.getPageNumber() == 0 && p.getPageSize() == 10
                && p.getSort().getOrderFor("nome").isDescending()));
        when(setorService.listar(isNull(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ControllerTestData.setor(1)), org.springframework.data.domain.PageRequest.of(1, 50), 51));
        mvc.perform(get("/setores").param("page", "1").param("size", "50"))
                .andExpect(status().isOk());
        verify(setorService).listar(isNull(), argThat(p -> p.getPageNumber() == 1 && p.getPageSize() == 50));
    }

    @Test
    void buscaAtualizaExcluiECategoriasDoSetor() throws Exception {
        when(setorService.buscarPorId(1L)).thenReturn(ControllerTestData.setor(1));
        when(setorService.atualizar(eq(1L), any(Setor.class))).thenReturn(ControllerTestData.setor(1));
        when(categoriaService.listarPorSetor(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(ControllerTestData.categoria(2, 1))));
        mvc.perform(get("/setores/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
        mvc.perform(put("/setores/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo nome\",\"sigla\":\"NT\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/setores/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/setores/1")).andExpect(status().isNoContent());
        verify(setorService).excluir(1L);
        mvc.perform(get("/setores/1/categorias")).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].setorId").value(1));
        when(setorService.buscarPorId(99L)).thenThrow(new RecursoNaoEncontradoException("Setor não encontrado."));
        mvc.perform(get("/setores/99")).andExpect(status().isNotFound());
        when(categoriaService.listarPorSetor(eq(99L), any(Pageable.class)))
                .thenThrow(new RecursoNaoEncontradoException("Setor não encontrado."));
        mvc.perform(get("/setores/99/categorias")).andExpect(status().isNotFound());
        doThrow(new RegraNegocioException("Setor possui categorias.")).when(setorService).excluir(2L);
        mvc.perform(delete("/setores/2")).andExpect(status().isConflict());
    }
}
