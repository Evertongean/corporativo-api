package br.edu.ifrn.sistemachamados.repository.specification;

import br.edu.ifrn.sistemachamados.model.Setor;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinâmicos de {@link Setor}.
 */
public final class SetorSpecification {

    private SetorSpecification() {
    }

    public static Specification<Setor> nomeContem(String nome) {
        return SpecificationSupport.nomeContemIgnoreCase("nome", nome);
    }
}
