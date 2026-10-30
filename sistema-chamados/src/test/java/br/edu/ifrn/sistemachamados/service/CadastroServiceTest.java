package br.edu.ifrn.sistemachamados.service;

import br.edu.ifrn.sistemachamados.exception.DadosInvalidosException;
import br.edu.ifrn.sistemachamados.exception.RegraNegocioException;
import br.edu.ifrn.sistemachamados.model.Categoria;
import br.edu.ifrn.sistemachamados.model.Setor;
import br.edu.ifrn.sistemachamados.model.Usuario;
import br.edu.ifrn.sistemachamados.model.enums.TipoUsuario;
import br.edu.ifrn.sistemachamados.repository.*;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CadastroServiceTest {
    @Test void emailDuplicadoETecnicoSemSetorSaoRejeitados() { UsuarioRepository usuarios = mock(UsuarioRepository.class); UsuarioService service = new UsuarioService(usuarios, mock(SetorRepository.class), mock(ChamadoRepository.class)); Usuario existente = usuario(1L, TipoUsuario.SOLICITANTE, null); Usuario duplicado = usuario(null, TipoUsuario.SOLICITANTE, null); duplicado.setNome("Novo"); duplicado.setEmail("a@b.com"); duplicado.setMatricula("2"); existente.setEmail("a@b.com"); when(usuarios.findByEmailIgnoreCase("a@b.com")).thenReturn(Optional.of(existente)); assertThrows(RegraNegocioException.class, () -> service.cadastrar(duplicado)); Usuario tecnico = usuario(null, TipoUsuario.TECNICO, null); tecnico.setNome("T"); tecnico.setEmail("t@b.com"); tecnico.setMatricula("3"); assertThrows(DadosInvalidosException.class, () -> service.cadastrar(tecnico)); }
    @Test void siglaDuplicadaECategoriaSemSetorSaoRejeitadas() { SetorRepository setores = mock(SetorRepository.class); SetorService setorService = new SetorService(setores, mock(CategoriaRepository.class), mock(ChamadoRepository.class)); Setor existente = new Setor(); existente.setId(1L); when(setores.findBySigla("TI")).thenReturn(Optional.of(existente)); Setor novo = new Setor(); novo.setNome("Tecnologia"); novo.setSigla("TI"); assertThrows(RegraNegocioException.class, () -> setorService.cadastrar(novo)); CategoriaService categoriaService = new CategoriaService(mock(CategoriaRepository.class), setores, mock(ChamadoRepository.class)); Categoria categoria = new Categoria(); categoria.setNome("Rede"); assertThrows(DadosInvalidosException.class, () -> categoriaService.cadastrar(categoria)); }
    @Test void exclusoesComChamadosSaoRejeitadas() { CategoriaRepository categorias = mock(CategoriaRepository.class); ChamadoRepository chamados = mock(ChamadoRepository.class); CategoriaService categoriaService = new CategoriaService(categorias, mock(SetorRepository.class), chamados); Categoria categoria = new Categoria(); categoria.setId(1L); when(categorias.findById(1L)).thenReturn(Optional.of(categoria)); when(chamados.existsByCategoriaId(1L)).thenReturn(true); assertThrows(RegraNegocioException.class, () -> categoriaService.excluir(1L)); SetorRepository setores = mock(SetorRepository.class); Setor setor = new Setor(); setor.setId(2L); when(setores.findById(2L)).thenReturn(Optional.of(setor)); when(chamados.existsByCategoria_Setor_Id(2L)).thenReturn(true); SetorService setorService = new SetorService(setores, categorias, chamados); assertThrows(RegraNegocioException.class, () -> setorService.excluir(2L)); }
    private static Usuario usuario(Long id, TipoUsuario tipo, Setor setor) { Usuario u = new Usuario(); u.setId(id); u.setTipo(tipo); u.setSetor(setor); return u; }
}
