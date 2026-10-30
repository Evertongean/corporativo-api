package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.repository.CategoriaRepository;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import br.edu.ifrn.sistemachamados.repository.specification.ChamadoSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final CategoriaRepository categoriaRepository;
    private final UsuarioRepository usuarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, CategoriaRepository categoriaRepository, UsuarioRepository usuarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.categoriaRepository = categoriaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Chamado abrirChamado(Chamado chamado) {
        if (chamado == null) {
            throw new DadosInvalidosException("Chamado é obrigatório.");
        }
        chamado.setTitulo(ServiceSupport.textoObrigatorio(chamado.getTitulo(), "Título"));
        chamado.setDescricao(ServiceSupport.textoObrigatorio(chamado.getDescricao(), "Descrição"));
        chamado.setLocal(ServiceSupport.textoObrigatorio(chamado.getLocal(), "Local"));

        Categoria categoria = buscarCategoria(idDaAssociacao(chamado.getCategoria(), "categoria"));
        Usuario solicitante = buscarUsuario(idDaAssociacao(chamado.getSolicitante(), "solicitante"));
        if (solicitante.getTipo() != TipoUsuario.SOLICITANTE) {
            throw new RegraNegocioException("Apenas solicitantes podem abrir chamados.");
        }

        chamado.setCategoria(categoria);
        chamado.setSolicitante(solicitante);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setPrioridade(chamado.getPrioridade() == null ? Prioridade.MEDIA : chamado.getPrioridade());
        chamado.setTecnico(null);
        chamado.setDataFechamento(null);
        chamado.setSolucao(null);
        return chamadoRepository.save(chamado);
    }

    @Transactional(readOnly = true)
    public Chamado buscarPorId(Long id) {
        ServiceSupport.idObrigatorio(id, "chamado");
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Chamado não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<Chamado> listar(
            StatusChamado status,
            Prioridade prioridade,
            Long setorId,
            Long categoriaId,
            Long solicitanteId,
            Long tecnicoId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim,
            Pageable pageable
    ) {
        ServiceSupport.validarPageable(pageable);
        validarFiltroId(setorId, "setor");
        validarFiltroId(categoriaId, "categoria");
        validarFiltroId(solicitanteId, "solicitante");
        validarFiltroId(tecnicoId, "técnico");
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new DadosInvalidosException("Data inicial não pode ser posterior à data final.");
        }
        return chamadoRepository.findAll(ChamadoSpecification.comFiltros(
                status, prioridade, setorId, categoriaId, solicitanteId, tecnicoId, dataInicio, dataFim
        ), pageable);
    }

    @Transactional
    public Chamado assumirChamado(Long chamadoId, Long tecnicoId) {
        Chamado chamado = buscarPorId(chamadoId);
        Usuario tecnico = buscarUsuario(tecnicoId);
        if (tecnico.getTipo() != TipoUsuario.TECNICO) {
            throw new RegraNegocioException("Apenas técnicos podem assumir chamados.");
        }
        if (chamado.getStatus() != StatusChamado.ABERTO) {
            throw new RegraNegocioException("Apenas chamados abertos podem ser assumidos.");
        }
        if (tecnico.getSetor() == null || chamado.getCategoria() == null || chamado.getCategoria().getSetor() == null
                || !Objects.equals(tecnico.getSetor().getId(), chamado.getCategoria().getSetor().getId())) {
            throw new RegraNegocioException("Técnico não pertence ao setor responsável pelo chamado.");
        }
        chamado.setTecnico(tecnico);
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO);
        return chamadoRepository.save(chamado);
    }

    @Transactional
    public Chamado resolverChamado(Long chamadoId, Long tecnicoId, String solucao) {
        Chamado chamado = buscarPorId(chamadoId);
        Usuario tecnico = buscarUsuario(tecnicoId);
        if (tecnico.getTipo() != TipoUsuario.TECNICO) {
            throw new RegraNegocioException("Apenas técnicos podem resolver chamados.");
        }
        if (chamado.getStatus() != StatusChamado.EM_ATENDIMENTO) {
            throw new RegraNegocioException("Apenas chamados em atendimento podem ser resolvidos.");
        }
        if (chamado.getTecnico() == null || !Objects.equals(chamado.getTecnico().getId(), tecnico.getId())) {
            throw new RegraNegocioException("Somente o técnico responsável pode resolver o chamado.");
        }
        chamado.setSolucao(ServiceSupport.textoObrigatorio(solucao, "Solução"));
        chamado.setStatus(StatusChamado.RESOLVIDO);
        chamado.setDataFechamento(LocalDateTime.now());
        return chamadoRepository.save(chamado);
    }

    @Transactional
    public Chamado cancelarChamado(Long chamadoId, Long solicitanteId) {
        Chamado chamado = buscarPorId(chamadoId);
        Usuario solicitante = buscarUsuario(solicitanteId);
        if (solicitante.getTipo() != TipoUsuario.SOLICITANTE
                || chamado.getSolicitante() == null
                || !Objects.equals(chamado.getSolicitante().getId(), solicitante.getId())) {
            throw new RegraNegocioException("Somente o solicitante responsável pode cancelar o chamado.");
        }
        if (chamado.getStatus() != StatusChamado.ABERTO) {
            throw new RegraNegocioException("Apenas chamados abertos podem ser cancelados.");
        }
        chamado.setStatus(StatusChamado.CANCELADO);
        return chamadoRepository.save(chamado);
    }

    private Long idDaAssociacao(Object associacao, String recurso) {
        Long id = associacao instanceof Categoria categoria ? categoria.getId()
                : associacao instanceof Usuario usuario ? usuario.getId() : null;
        return ServiceSupport.idObrigatorio(id, recurso);
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(ServiceSupport.idObrigatorio(id, "categoria"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(ServiceSupport.idObrigatorio(id, "usuário"))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    private void validarFiltroId(Long id, String recurso) {
        if (id != null) {
            ServiceSupport.idObrigatorio(id, recurso);
        }
    }
}
