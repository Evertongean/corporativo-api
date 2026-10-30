package br.edu.ifrn.sistemachamados.dto.request;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade; import jakarta.validation.constraints.*;
public record AbrirChamadoRequest(@NotBlank @Size(max=200) String titulo,@NotBlank String descricao,@NotBlank @Size(max=200) String local,Prioridade prioridade,@NotNull @Positive Long solicitanteId,@NotNull @Positive Long categoriaId) {}
