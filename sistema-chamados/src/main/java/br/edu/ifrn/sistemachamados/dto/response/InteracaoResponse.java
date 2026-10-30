package br.edu.ifrn.sistemachamados.dto.response; import java.time.*; public record InteracaoResponse(Long id,String texto,LocalDateTime dataHora,Long chamadoId,Long autorId) {}
