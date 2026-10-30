package br.edu.ifrn.sistemachamados.dto.request;
import jakarta.validation.constraints.*;
public record CategoriaRequest(@NotBlank @Size(max=150) String nome, String descricao, @NotNull @Positive Long setorId) {}
