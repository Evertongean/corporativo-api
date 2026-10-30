package br.edu.ifrn.sistemachamados.repository.specification;

import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Filtros dinâmicos de {@link Chamado}.
 */
public final class ChamadoSpecification {

    private ChamadoSpecification() {
    }

    public static Specification<Chamado> comFiltros(
            StatusChamado status,
            Prioridade prioridade,
            Long setorId,
            Long categoriaId,
            Long solicitanteId,
            Long tecnicoId,
            LocalDateTime dataInicio,
            LocalDateTime dataFim
    ) {
        return statusIgual(status)
                .and(prioridadeIgual(prioridade))
                .and(setorIdIgual(setorId))
                .and(categoriaIdIgual(categoriaId))
                .and(solicitanteIdIgual(solicitanteId))
                .and(tecnicoIdIgual(tecnicoId))
                .and(dataAberturaAPartirDe(dataInicio))
                .and(dataAberturaAte(dataFim));
    }

    public static Specification<Chamado> statusIgual(StatusChamado status) {
        return SpecificationSupport.igual("status", status);
    }

    public static Specification<Chamado> prioridadeIgual(Prioridade prioridade) {
        return SpecificationSupport.igual("prioridade", prioridade);
    }

    public static Specification<Chamado> setorIdIgual(Long setorId) {
        if (setorId == null) {
            return SpecificationSupport.semFiltro();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                SpecificationSupport.caminho(root, "categoria", "setor", "id"), setorId
        );
    }

    public static Specification<Chamado> categoriaIdIgual(Long categoriaId) {
        return SpecificationSupport.idDaAssociacaoIgual("categoria", categoriaId);
    }

    public static Specification<Chamado> solicitanteIdIgual(Long solicitanteId) {
        return SpecificationSupport.idDaAssociacaoIgual("solicitante", solicitanteId);
    }

    public static Specification<Chamado> tecnicoIdIgual(Long tecnicoId) {
        return SpecificationSupport.idDaAssociacaoIgual("tecnico", tecnicoId);
    }

    public static Specification<Chamado> dataAberturaAPartirDe(LocalDateTime dataInicio) {
        return SpecificationSupport.dataMaiorOuIgual("dataAbertura", dataInicio);
    }

    public static Specification<Chamado> dataAberturaAte(LocalDateTime dataFim) {
        return SpecificationSupport.dataMenorOuIgual("dataAbertura", dataFim);
    }
}
