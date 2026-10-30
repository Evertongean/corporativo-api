package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import org.springframework.data.domain.Pageable;

import java.util.regex.Pattern;

final class ServiceSupport {

    private static final int TAMANHO_MAXIMO_PAGINA = 50;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ServiceSupport() {
    }

    static String textoObrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException(campo + " é obrigatório.");
        }
        return valor.trim();
    }

    static void validarEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new DadosInvalidosException("E-mail inválido.");
        }
    }

    static void validarPageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            throw new DadosInvalidosException("Paginação é obrigatória.");
        }
        if (pageable.getPageNumber() < 0 || pageable.getPageSize() < 1 || pageable.getPageSize() > TAMANHO_MAXIMO_PAGINA) {
            throw new DadosInvalidosException("O tamanho da página deve estar entre 1 e " + TAMANHO_MAXIMO_PAGINA + ".");
        }
    }

    static Long idObrigatorio(Long id, String recurso) {
        if (id == null || id <= 0) {
            throw new DadosInvalidosException("Identificador de " + recurso + " inválido.");
        }
        return id;
    }
}
