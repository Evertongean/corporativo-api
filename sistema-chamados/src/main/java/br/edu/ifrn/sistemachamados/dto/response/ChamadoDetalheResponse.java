package br.edu.ifrn.sistemachamados.dto.response; import java.util.*; public record ChamadoDetalheResponse(ChamadoResponse chamado,List<InteracaoResponse> interacoes) {}
