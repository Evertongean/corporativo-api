package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.exception.RecursoNaoEncontradoException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.repository.CategoriaRepository;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.SetorRepository;
import br.edu.ifrn.sistemachamados.repository.specification.CategoriaSpecification;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final SetorRepository setorRepository;
    private final ChamadoRepository chamadoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, SetorRepository setorRepository, ChamadoRepository chamadoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.setorRepository = setorRepository;
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional
    public Categoria cadastrar(Categoria categoria) {
        preencherDadosValidos(categoria);
        return categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        ServiceSupport.idObrigatorio(id, "categoria");
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada."));
    }

    @Transactional(readOnly = true)
    public Page<Categoria> listar(String nome, Long setorId, Pageable pageable) {
        ServiceSupport.validarPageable(pageable);
        if (setorId != null) {
            ServiceSupport.idObrigatorio(setorId, "setor");
        }
        return categoriaRepository.findAll(CategoriaSpecification.comFiltros(nome, setorId), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Categoria> listarPorSetor(Long setorId, Pageable pageable) {
        ServiceSupport.idObrigatorio(setorId, "setor");
        ServiceSupport.validarPageable(pageable);
        buscarSetor(setorId);
        return categoriaRepository.findBySetorId(setorId, pageable);
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria dados) {
        Categoria categoria = buscarPorId(id);
        preencherDadosValidos(dados);
        categoria.setNome(dados.getNome());
        categoria.setDescricao(dados.getDescricao());
        categoria.setSetor(dados.getSetor());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarPorId(id);
        if (chamadoRepository.existsByCategoriaId(categoria.getId())) {
            throw new RegraNegocioException("Não é possível excluir categoria com chamados vinculados.");
        }
        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new RegraNegocioException("Não é possível excluir categoria com registros vinculados.");
        }
    }

    private void preencherDadosValidos(Categoria categoria) {
        if (categoria == null) {
            throw new DadosInvalidosException("Categoria é obrigatória.");
        }
        categoria.setNome(ServiceSupport.textoObrigatorio(categoria.getNome(), "Nome"));
        if (categoria.getDescricao() != null) {
            categoria.setDescricao(categoria.getDescricao().trim());
        }
        if (categoria.getSetor() == null || categoria.getSetor().getId() == null) {
            throw new DadosInvalidosException("Setor é obrigatório.");
        }
        categoria.setSetor(buscarSetor(categoria.getSetor().getId()));
    }

    private Setor buscarSetor(Long setorId) {
        ServiceSupport.idObrigatorio(setorId, "setor");
        return setorRepository.findById(setorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Setor não encontrado."));
    }
}
