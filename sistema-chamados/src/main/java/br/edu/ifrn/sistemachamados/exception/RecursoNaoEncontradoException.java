package br.edu.ifrn.sistemachamados.exception;

/** Indica que um recurso solicitado não foi encontrado. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
