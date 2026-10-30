package br.edu.ifrn.sistemachamados.dto.request; import jakarta.validation.constraints.*; public record ResolverChamadoRequest(@NotNull @Positive Long tecnicoId,@NotBlank String solucao) {}
