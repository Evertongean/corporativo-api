package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Operações de persistência e consultas de {@link Chamado}.
 */
public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    Page<Chamado> findBySolicitanteId(Long solicitanteId, Pageable pageable);

    Page<Chamado> findBySolicitanteIdAndStatus(Long solicitanteId, StatusChamado status, Pageable pageable);

    Page<Chamado> findByTecnicoId(Long tecnicoId, Pageable pageable);

    Page<Chamado> findByTecnicoIdAndStatus(Long tecnicoId, StatusChamado status, Pageable pageable);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByCategoria_Setor_Id(Long setorId);
}
