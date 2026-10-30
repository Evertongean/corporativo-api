package br.edu.ifrn.sistemachamados.repository;

import br.edu.ifrn.sistemachamados.model.Interacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Operações de persistência e consultas de {@link Interacao}.
 */
public interface InteracaoRepository extends JpaRepository<Interacao, Long> {

    Page<Interacao> findByChamadoIdOrderByDataHoraAsc(Long chamadoId, Pageable pageable);
}
