package br.edu.ifrn.sistemachamados.dto.request;
import jakarta.validation.constraints.*;
public record SetorRequest(@NotBlank @Size(max=150) String nome, @NotBlank @Size(max=20) String sigla) {}
