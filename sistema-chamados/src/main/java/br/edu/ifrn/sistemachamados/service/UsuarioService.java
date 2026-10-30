package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.SetorRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import br.edu.ifrn.sistemachamados.repository.specification.UsuarioSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final SetorRepository setorRepository;
    private final ChamadoRepository chamadoRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, SetorRepository setorRepository, ChamadoRepository chamadoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.setorRepository = setorRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public Usuario cadastrar(Usuario usuario) {
        preencherDadosValidos(usuario);
        verificarEmailDisponivel(usuario.getEmail(), null);
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        ServiceSupport.idObrigatorio(id, "usuário");
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(String nome, TipoUsuario tipo, Long setorId, Pageable pageable) {
        ServiceSupport.validarPageable(pageable);
        if (setorId != null) {
            ServiceSupport.idObrigatorio(setorId, "setor");
        }
        return usuarioRepository.findAll(UsuarioSpecification.comFiltros(nome, tipo, setorId), pageable);
    }

    @Transactional
    public Usuario atualizar(Long id, Usuario dados) {
        Usuario usuario = buscarPorId(id);
        preencherDadosValidos(dados);
        verificarEmailDisponivel(dados.getEmail(), usuario.getId());
        usuario.setNome(dados.getNome());
        usuario.setEmail(dados.getEmail());
        usuario.setMatricula(dados.getMatricula());
        usuario.setTipo(dados.getTipo());
        usuario.setSetor(dados.getSetor());
        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Page<Chamado> listarChamados(Long usuarioId, StatusChamado status, Pageable pageable) {
        Usuario usuario = buscarPorId(usuarioId);
        ServiceSupport.validarPageable(pageable);
        if (usuario.getTipo() == TipoUsuario.SOLICITANTE) {
            return status == null
                    ? chamadoRepository.findBySolicitanteId(usuario.getId(), pageable)
                    : chamadoRepository.findBySolicitanteIdAndStatus(usuario.getId(), status, pageable);
        }
        return status == null
                ? chamadoRepository.findByTecnicoId(usuario.getId(), pageable)
                : chamadoRepository.findByTecnicoIdAndStatus(usuario.getId(), status, pageable);
    }

    private void preencherDadosValidos(Usuario usuario) {
        if (usuario == null) {
            throw new DadosInvalidosException("Usuário é obrigatório.");
        }
        usuario.setNome(ServiceSupport.textoObrigatorio(usuario.getNome(), "Nome"));
        usuario.setEmail(ServiceSupport.textoObrigatorio(usuario.getEmail(), "E-mail").toLowerCase());
        ServiceSupport.validarEmail(usuario.getEmail());
        usuario.setMatricula(ServiceSupport.textoObrigatorio(usuario.getMatricula(), "Matrícula"));
        if (usuario.getTipo() == null) {
            throw new DadosInvalidosException("Tipo é obrigatório.");
        }
        if (usuario.getTipo() == TipoUsuario.TECNICO) {
            if (usuario.getSetor() == null || usuario.getSetor().getId() == null) {
                throw new DadosInvalidosException("Técnico deve possuir setor.");
            }
            usuario.setSetor(buscarSetor(usuario.getSetor().getId()));
        } else if (usuario.getTipo() == TipoUsuario.SOLICITANTE) {
            usuario.setSetor(null);
        } else {
            throw new DadosInvalidosException("Tipo de usuário inválido.");
        }
    }

    private Setor buscarSetor(Long setorId) {
        ServiceSupport.idObrigatorio(setorId, "setor");
        return setorRepository.findById(setorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado."));
    }

    private void verificarEmailDisponivel(String email, Long idAtual) {
        usuarioRepository.findByEmailIgnoreCase(email)
                .filter(existente -> !Objects.equals(existente.getId(), idAtual))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe um usuário com este e-mail.");
                });
    }
}
