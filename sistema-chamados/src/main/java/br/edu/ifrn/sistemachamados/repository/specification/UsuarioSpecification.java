package br.edu.ifrn.sistemachamados.repository.specification;

import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinâmicos de {@link Usuario}.
 */
public final class UsuarioSpecification {

    private UsuarioSpecification() {
    }

    public static Specification<Usuario> comFiltros(String nome, TipoUsuario tipo, Long setorId) {
        return nomeContem(nome)
                .and(tipoIgual(tipo))
                .and(setorIdIgual(setorId));
    }

    public static Specification<Usuario> nomeContem(String nome) {
        return SpecificationSupport.nomeContemIgnoreCase("nome", nome);
    }

    public static Specification<Usuario> tipoIgual(TipoUsuario tipo) {
        return SpecificationSupport.igual("tipo", tipo);
    }

    public static Specification<Usuario> setorIdIgual(Long setorId) {
        return SpecificationSupport.idDaAssociacaoIgual("setor", setorId);
    }
}
