package br.edu.ifrn.sistemachamados.exception;

/** Indica que os dados recebidos em uma operação são inválidos. */
public class DadosInvalidosException extends RuntimeException {

    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }
}
