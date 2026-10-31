package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Interacao;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;

import java.time.LocalDateTime;

final class ControllerTestData {
    private ControllerTestData() {
    }

    static Setor setor(long id) {
        Setor setor = new Setor();
        setor.setId(id);
        setor.setNome("Tecnologia");
        setor.setSigla("TI");
        return setor;
    }

    static Categoria categoria(long id, long setorId) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome("Rede");
        categoria.setDescricao("Suporte de rede");
        categoria.setSetor(setor(setorId));
        return categoria;
    }

    static Usuario usuario(long id, TipoUsuario tipo, Long setorId) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuário Teste");
        usuario.setEmail("usuario" + id + "@example.com");
        usuario.setMatricula("2026000" + id);
        usuario.setTipo(tipo);
        usuario.setSetor(setorId == null ? null : setor(setorId));
        return usuario;
    }

    static Chamado chamado(long id) {
        Chamado chamado = new Chamado();
        chamado.setId(id);
        chamado.setTitulo("Sem internet");
        chamado.setDescricao("Internet indisponível");
        chamado.setLocal("Sala 1");
        chamado.setPrioridade(Prioridade.ALTA);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.of(2026, 1, 2, 10, 0));
        chamado.setSolicitante(usuario(3, TipoUsuario.SOLICITANTE, null));
        chamado.setCategoria(categoria(2, 1));
        return chamado;
    }

    static Interacao interacao(long id, long chamadoId, long autorId, int minuto) {
        Interacao interacao = new Interacao();
        interacao.setId(id);
        interacao.setTexto("Atualização " + id);
        interacao.setDataHora(LocalDateTime.of(2026, 1, 2, 10, minuto));
        Chamado chamado = chamado(chamadoId);
        interacao.setChamado(chamado);
        interacao.setAutor(usuario(autorId, TipoUsuario.SOLICITANTE, null));
        return interacao;
    }
}
