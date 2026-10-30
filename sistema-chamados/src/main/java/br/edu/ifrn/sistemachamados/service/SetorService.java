package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.repository.CategoriaRepository;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.SetorRepository;
import br.edu.ifrn.sistemachamados.repository.specification.SetorSpecification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final CategoriaRepository categoriaRepository;
    private final ChamadoRepository chamadoRepository;

    public SetorService(SetorRepository setorRepository, CategoriaRepository categoriaRepository, ChamadoRepository chamadoRepository) {
        this.setorRepository = setorRepository;
        this.categoriaRepository = categoriaRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public Setor cadastrar(Setor setor) {
        validarSetor(setor);
        verificarSiglaDisponivel(setor.getSigla(), null);
        return setorRepository.save(setor);
    }

    @Transactional(readOnly = true)
    public Setor buscarPorId(Long id) {
        ServiceSupport.idObrigatorio(id, "setor");
        return setorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado."));
    }

    @Transactional(readOnly = true)
    public Page<Setor> listar(String nome, Pageable pageable) {
        ServiceSupport.validarPageable(pageable);
        return setorRepository.findAll(SetorSpecification.nomeContem(nome), pageable);
    }

    @Transactional
    public Setor atualizar(Long id, Setor dados) {
        Setor setor = buscarPorId(id);
        validarSetor(dados);
        verificarSiglaDisponivel(dados.getSigla(), setor.getId());
        setor.setNome(dados.getNome());
        setor.setSigla(dados.getSigla());
        return setorRepository.save(setor);
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = buscarPorId(id);
        if (chamadoRepository.existsByCategoria_Setor_Id(setor.getId())) {
            throw new RegraNegocioException("Não é possível excluir setor com chamados vinculados.");
        }
        try {
            setorRepository.delete(setor);
            setorRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            String mensagem = categoriaRepository.existsBySetorId(setor.getId())
                    ? "Não é possível excluir setor com categorias vinculadas."
                    : "Não é possível excluir setor com registros vinculados.";
            throw new RegraNegocioException(mensagem);
        }
    }

    private void validarSetor(Setor setor) {
        if (setor == null) {
            throw new br.edu.ifrn.sistemachamados.exception.DadosInvalidosException("Setor é obrigatório.");
        }
        setor.setNome(ServiceSupport.textoObrigatorio(setor.getNome(), "Nome"));
        setor.setSigla(ServiceSupport.textoObrigatorio(setor.getSigla(), "Sigla"));
    }

    private void verificarSiglaDisponivel(String sigla, Long idAtual) {
        setorRepository.findBySigla(sigla)
                .filter(existente -> !Objects.equals(existente.getId(), idAtual))
                .ifPresent(existente -> {
                    throw new RegraNegocioException("Já existe um setor com esta sigla.");
                });
    }
}
