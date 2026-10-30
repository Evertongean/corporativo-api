package br.edu.ifrn.sistemachamados.exception;

/** Indica uma violação das regras de negócio do sistema. */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
