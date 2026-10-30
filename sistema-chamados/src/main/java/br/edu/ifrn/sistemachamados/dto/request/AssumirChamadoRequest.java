package br.edu.ifrn.sistemachamados.dto.request; import jakarta.validation.constraints.*; public record AssumirChamadoRequest(@NotNull @Positive Long tecnicoId) {}
