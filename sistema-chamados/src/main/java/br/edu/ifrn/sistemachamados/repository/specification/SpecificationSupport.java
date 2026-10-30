package br.edu.ifrn.sistemachamados.repository.specification;

import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * Predicados reutilizáveis para as Specifications do repositório.
 */
final class SpecificationSupport {

    private SpecificationSupport() {
    }

    static <T> Specification<T> semFiltro() {
        return (root, query, criteriaBuilder) -> null;
    }

    static <T> Specification<T> nomeContemIgnoreCase(String atributo, String valor) {
        if (!temTexto(valor)) {
            return semFiltro();
        }

        String valorNormalizado = valor.trim().toLowerCase(Locale.ROOT);
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get(atributo)),
                "%" + valorNormalizado + "%"
        );
    }

    static <T, V> Specification<T> igual(String atributo, V valor) {
        if (valor == null) {
            return semFiltro();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(atributo), valor);
    }

    static <T> Specification<T> idDaAssociacaoIgual(String associacao, Long id) {
        if (id == null) {
            return semFiltro();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(associacao).get("id"), id);
    }

    static <T> Specification<T> dataMaiorOuIgual(String atributo, LocalDateTime data) {
        if (data == null) {
            return semFiltro();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
                root.<LocalDateTime>get(atributo), data
        );
    }

    static <T> Specification<T> dataMenorOuIgual(String atributo, LocalDateTime data) {
        if (data == null) {
            return semFiltro();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
                root.<LocalDateTime>get(atributo), data
        );
    }

    static <T> Path<?> caminho(Path<T> origem, String... atributos) {
        Path<?> caminho = origem;
        for (String atributo : atributos) {
            caminho = caminho.get(atributo);
        }
        return caminho;
    }

    static boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }
}
