package br.edu.ifrn.sistemachamados.controller;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

final class WebSupport {
    private WebSupport() {
    }

    static Pageable page(Integer page, Integer size, String[] sort, String... camposPermitidos) {
        int pagina = page == null ? 0 : page;
        int tamanho = size == null ? 10 : size;
        if (pagina < 0 || tamanho < 1 || tamanho > 50) {
            throw new DadosInvalidosException("Parâmetros de paginação inválidos.");
        }

        Set<String> campos = Set.of(camposPermitidos);
        Sort ordenacao = Sort.unsorted();
        if (sort != null) {
            for (int indice = 0; indice < sort.length; indice++) {
                String[] partes = sort[indice].split(",", -1);
                if (partes.length == 1 && indice + 1 < sort.length
                        && ("asc".equalsIgnoreCase(sort[indice + 1]) || "desc".equalsIgnoreCase(sort[indice + 1]))) {
                    partes = new String[]{partes[0], sort[++indice]};
                }
                if (partes.length < 1 || partes.length > 2 || partes[0].isBlank() || !campos.contains(partes[0])) {
                    throw new DadosInvalidosException("Ordenação inválida.");
                }
                try {
                    ordenacao = ordenacao.and(Sort.by(partes.length == 2
                            ? Sort.Direction.fromString(partes[1]) : Sort.Direction.ASC, partes[0]));
                } catch (IllegalArgumentException exception) {
                    throw new DadosInvalidosException("Ordenação inválida.");
                }
            }
        }
        return PageRequest.of(pagina, tamanho, ordenacao);
    }
}
