package br.edu.ifrn.sistemachamados.dto.request;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario; import jakarta.validation.constraints.*;
public record UsuarioRequest(@NotBlank @Size(max=150) String nome,@NotBlank @Email @Size(max=255) String email,@NotBlank @Size(max=30) String matricula,@NotNull TipoUsuario tipo,@Positive Long setorId) {}
