package br.edu.ifrn.sistemachamados.repository.specification;

import br.edu.ifrn.sistemachamados.model.Categoria;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinâmicos de {@link Categoria}.
 */
public final class CategoriaSpecification {

    private CategoriaSpecification() {
    }

    public static Specification<Categoria> comFiltros(String nome, Long setorId) {
        return nomeContem(nome).and(setorIdIgual(setorId));
    }

    public static Specification<Categoria> nomeContem(String nome) {
        return SpecificationSupport.nomeContemIgnoreCase("nome", nome);
    }

    public static Specification<Categoria> setorIdIgual(Long setorId) {
        return SpecificationSupport.idDaAssociacaoIgual("setor", setorId);
    }
}
