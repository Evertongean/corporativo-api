package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Setor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

/**
 * Operações de persistência e consultas de {@link Setor}.
 */
public interface SetorRepository extends JpaRepository<Setor, Long>, JpaSpecificationExecutor<Setor> {

    Optional<Setor> findBySigla(String sigla);

    boolean existsBySiglaIgnoreCase(String sigla);
}
