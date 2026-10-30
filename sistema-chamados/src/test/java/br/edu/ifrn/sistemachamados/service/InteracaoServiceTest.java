package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Interacao;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.InteracaoRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteracaoServiceTest {
    @Mock private InteracaoRepository interacaoRepository; @Mock private ChamadoRepository chamadoRepository; @Mock private UsuarioRepository usuarioRepository;
    private InteracaoService service; private Chamado chamado; private Usuario solicitante; private Usuario tecnico; private Usuario estranho;
    @BeforeEach void setup() { service = new InteracaoService(interacaoRepository, chamadoRepository, usuarioRepository); solicitante = usuario(1L, TipoUsuario.SOLICITANTE); tecnico = usuario(2L, TipoUsuario.TECNICO); estranho = usuario(3L, TipoUsuario.TECNICO); chamado = new Chamado(); chamado.setId(10L); chamado.setStatus(StatusChamado.ABERTO); chamado.setSolicitante(solicitante); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); lenient().when(interacaoRepository.save(any())).thenAnswer(i -> i.getArgument(0)); }
    @Test void solicitanteRegistraInteracaoComDataAutomatica() { when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante)); Interacao interacao = service.registrarInteracao(10L, 1L, "Mensagem"); assertAll(() -> assertSame(solicitante, interacao.getAutor()), () -> assertSame(chamado, interacao.getChamado()), () -> assertNotNull(interacao.getDataHora())); }
    @Test void tecnicoAtribuidoPodeInteragir() { chamado.setTecnico(tecnico); when(usuarioRepository.findById(2L)).thenReturn(Optional.of(tecnico)); assertDoesNotThrow(() -> service.registrarInteracao(10L, 2L, "Atendendo")); }
    @Test void estranhoETecnicoNaoAtribuidoNaoPodemInteragir() { when(usuarioRepository.findById(3L)).thenReturn(Optional.of(estranho)); when(usuarioRepository.findById(2L)).thenReturn(Optional.of(tecnico)); assertThrows(RegraNegocioException.class, () -> service.registrarInteracao(10L, 3L, "x")); assertThrows(RegraNegocioException.class, () -> service.registrarInteracao(10L, 2L, "x")); }
    @Test void chamadosEncerradosNaoAceitamInteracao() { when(usuarioRepository.findById(1L)).thenReturn(Optional.of(solicitante)); chamado.setStatus(StatusChamado.RESOLVIDO); assertThrows(RegraNegocioException.class, () -> service.registrarInteracao(10L, 1L, "x")); chamado.setStatus(StatusChamado.CANCELADO); assertThrows(RegraNegocioException.class, () -> service.registrarInteracao(10L, 1L, "x")); }
    private static Usuario usuario(Long id, TipoUsuario tipo) { Usuario u = new Usuario(); u.setId(id); u.setTipo(tipo); return u; }
}
