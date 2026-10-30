package br.edu.ifrn.sistemachamados.dto.request; import jakarta.validation.constraints.*; public record CriarInteracaoRequest(@NotNull @Positive Long autorId,@NotBlank String texto) {}
