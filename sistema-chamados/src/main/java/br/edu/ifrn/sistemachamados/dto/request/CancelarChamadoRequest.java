package br.edu.ifrn.sistemachamados.dto.request; import jakarta.validation.constraints.*; public record CancelarChamadoRequest(@NotNull @Positive Long solicitanteId) {}
