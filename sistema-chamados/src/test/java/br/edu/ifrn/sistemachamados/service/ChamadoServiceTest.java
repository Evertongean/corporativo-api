package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.model.Chamado;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.Prioridade;
import br.edu.ifrn.sistemachamados.model.enums.StatusChamado;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.repository.CategoriaRepository;
import br.edu.ifrn.sistemachamados.repository.ChamadoRepository;
import br.edu.ifrn.sistemachamados.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {
    @Mock private ChamadoRepository chamadoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    private ChamadoService service;
    private Setor setor;
    private Categoria categoria;
    private Usuario solicitante;
    private Usuario tecnico;

    @BeforeEach
    void setUp() {
        service = new ChamadoService(chamadoRepository, categoriaRepository, usuarioRepository);
        setor = setor(1L); categoria = categoria(2L, setor); solicitante = usuario(3L, TipoUsuario.SOLICITANTE, null); tecnico = usuario(4L, TipoUsuario.TECNICO, setor);
        lenient().when(chamadoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test void solicitanteAbreChamadoComEstadoInicialControladoPeloSistema() {
        Chamado chamado = novoChamado(); chamado.setStatus(StatusChamado.RESOLVIDO); chamado.setPrioridade(null); chamado.setTecnico(tecnico); chamado.setDataFechamento(LocalDateTime.now()); chamado.setSolucao("ignorar");
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria)); when(usuarioRepository.findById(3L)).thenReturn(Optional.of(solicitante));
        Chamado salvo = service.abrirChamado(chamado);
        assertAll(() -> assertEquals(StatusChamado.ABERTO, salvo.getStatus()), () -> assertEquals(Prioridade.MEDIA, salvo.getPrioridade()), () -> assertNotNull(salvo.getDataAbertura()), () -> assertNull(salvo.getTecnico()), () -> assertNull(salvo.getDataFechamento()), () -> assertNull(salvo.getSolucao()));
        verify(chamadoRepository).save(chamado);
    }

    @Test void tecnicoNaoPodeAbrirChamado() {
        Chamado chamado = novoChamado(); chamado.setSolicitante(tecnico);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria)); when(usuarioRepository.findById(4L)).thenReturn(Optional.of(tecnico));
        assertThrows(RegraNegocioException.class, () -> service.abrirChamado(chamado)); verify(chamadoRepository, never()).save(any());
    }

    @Test void tecnicoDoSetorAssumeChamadoAberto() {
        Chamado chamado = chamado(StatusChamado.ABERTO); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(4L)).thenReturn(Optional.of(tecnico));
        service.assumirChamado(10L, 4L);
        assertAll(() -> assertEquals(StatusChamado.EM_ATENDIMENTO, chamado.getStatus()), () -> assertSame(tecnico, chamado.getTecnico()));
    }

    @Test void tecnicoDeOutroSetorNaoAssume() {
        Chamado chamado = chamado(StatusChamado.ABERTO); Usuario outro = usuario(5L, TipoUsuario.TECNICO, setor(9L)); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(5L)).thenReturn(Optional.of(outro));
        assertThrows(RegraNegocioException.class, () -> service.assumirChamado(10L, 5L)); verify(chamadoRepository, never()).save(any());
    }

    @Test void chamadoJaAssumidoNaoPodeSerAssumidoNovamente() {
        Chamado chamado = chamado(StatusChamado.EM_ATENDIMENTO); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(4L)).thenReturn(Optional.of(tecnico));
        assertThrows(RegraNegocioException.class, () -> service.assumirChamado(10L, 4L));
    }

    @Test void tecnicoResponsavelResolveChamado() {
        Chamado chamado = chamado(StatusChamado.EM_ATENDIMENTO); chamado.setTecnico(tecnico); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(4L)).thenReturn(Optional.of(tecnico));
        service.resolverChamado(10L, 4L, "Troca concluída");
        assertAll(() -> assertEquals(StatusChamado.RESOLVIDO, chamado.getStatus()), () -> assertEquals("Troca concluída", chamado.getSolucao()), () -> assertNotNull(chamado.getDataFechamento()));
    }

    @Test void outroTecnicoESolucaoVaziaImpedemResolucao() {
        Chamado chamado = chamado(StatusChamado.EM_ATENDIMENTO); chamado.setTecnico(tecnico); Usuario outro = usuario(5L, TipoUsuario.TECNICO, setor); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(5L)).thenReturn(Optional.of(outro)); when(usuarioRepository.findById(4L)).thenReturn(Optional.of(tecnico));
        assertThrows(RegraNegocioException.class, () -> service.resolverChamado(10L, 5L, "ok")); assertThrows(DadosInvalidosException.class, () -> service.resolverChamado(10L, 4L, " "));
    }

    @Test void solicitanteCancelaAbertoMasNaoAtendimentoOuChamadoDeOutro() {
        Chamado chamado = chamado(StatusChamado.ABERTO); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(3L)).thenReturn(Optional.of(solicitante)); service.cancelarChamado(10L, 3L); assertEquals(StatusChamado.CANCELADO, chamado.getStatus());
        chamado.setStatus(StatusChamado.EM_ATENDIMENTO); assertThrows(RegraNegocioException.class, () -> service.cancelarChamado(10L, 3L));
    }

    @Test void outroSolicitanteNaoCancelaChamado() {
        Chamado chamado = chamado(StatusChamado.ABERTO); Usuario outro = usuario(6L, TipoUsuario.SOLICITANTE, null); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(6L)).thenReturn(Optional.of(outro));
        assertThrows(RegraNegocioException.class, () -> service.cancelarChamado(10L, 6L));
    }

    @Test void chamadoResolvidoNaoSofreNovasTransicoes() {
        Chamado chamado = chamado(StatusChamado.RESOLVIDO); chamado.setTecnico(tecnico); when(chamadoRepository.findById(10L)).thenReturn(Optional.of(chamado)); when(usuarioRepository.findById(4L)).thenReturn(Optional.of(tecnico)); when(usuarioRepository.findById(3L)).thenReturn(Optional.of(solicitante));
        assertAll(() -> assertThrows(RegraNegocioException.class, () -> service.assumirChamado(10L, 4L)), () -> assertThrows(RegraNegocioException.class, () -> service.resolverChamado(10L, 4L, "x")), () -> assertThrows(RegraNegocioException.class, () -> service.cancelarChamado(10L, 3L)));
    }

    private Chamado novoChamado() { Chamado c = new Chamado(); c.setTitulo("Sem rede"); c.setDescricao("Internet indisponível"); c.setLocal("Sala 1"); c.setCategoria(categoria); c.setSolicitante(solicitante); return c; }
    private Chamado chamado(StatusChamado status) { Chamado c = novoChamado(); c.setId(10L); c.setStatus(status); return c; }
    private static Setor setor(Long id) { Setor s = new Setor(); s.setId(id); return s; }
    private static Categoria categoria(Long id, Setor setor) { Categoria c = new Categoria(); c.setId(id); c.setSetor(setor); return c; }
    private static Usuario usuario(Long id, TipoUsuario tipo, Setor setor) { Usuario u = new Usuario(); u.setId(id); u.setTipo(tipo); u.setSetor(setor); return u; }
}
