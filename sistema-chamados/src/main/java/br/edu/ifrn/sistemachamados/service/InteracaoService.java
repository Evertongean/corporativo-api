package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Interacao;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.InteracaoRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class InteracaoService {

    private final InteracaoRepository interacaoRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    public InteracaoService(InteracaoRepository interacaoRepository, ChamadoRepository chamadoRepository, UsuarioRepository usuarioRepository) {
        this.interacaoRepository = interacaoRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Interacao registrarInteracao(Long chamadoId, Long autorId, String texto) {
        Chamado chamado = buscarChamado(chamadoId);
        Usuario autor = buscarUsuario(autorId);
        if (chamado.getStatus() == StatusChamado.RESOLVIDO || chamado.getStatus() == StatusChamado.CANCELADO) {
            throw new RegraNegocioException("Não é possível registrar interações em chamado encerrado.");
        }
        if (!autorPermitido(chamado, autor)) {
            throw new RegraNegocioException("Usuário não possui permissão para interagir neste chamado.");
        }

        Interacao interacao = new Interacao();
        interacao.setTexto(ServiceSupport.textoObrigatorio(texto, "Texto"));
        interacao.setChamado(chamado);
        interacao.setAutor(autor);
        interacao.setDataHora(LocalDateTime.now());
        return interacaoRepository.save(interacao);
    }

    @Transactional(readOnly = true)
    public Page<Interacao> listarPorChamado(Long chamadoId, Pageable pageable) {
        buscarChamado(chamadoId);
        return interacaoRepository.findByChamadoIdOrderByDataHoraAsc(chamadoId, pageable);
    }

    private boolean autorPermitido(Chamado chamado, Usuario autor) {
        return (chamado.getSolicitante() != null && Objects.equals(chamado.getSolicitante().getId(), autor.getId()))
                || (chamado.getTecnico() != null && Objects.equals(chamado.getTecnico().getId(), autor.getId()));
    }

    private Chamado buscarChamado(Long id) {
        return chamadoRepository.findById(ServiceSupport.idObrigatorio(id, "chamado"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado."));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(ServiceSupport.idObrigatorio(id, "usuário"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }
}
