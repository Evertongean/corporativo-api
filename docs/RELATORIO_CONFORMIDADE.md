# Relatório de Conformidade — Sistema de Chamados IFRN

Data da auditoria: 30 de outubro de 2026. Fonte funcional utilizada: especificação detalhada da atividade. O arquivo `Projeto_API_Sistema_de_Chamados_IFRN.pdf` não estava acessível no workspace durante a auditoria; por isso, essa fonte específica está registrada como não verificada.

| Categoria | Requisito | Status | Evidência | Observações |
| --- | --- | --- | --- | --- |
| Stack tecnológica | Java 21, Spring Boot 4.1.2 e Maven | ATENDE | `sistema-chamados/pom.xml`: `java.version=21` e parent `4.1.2`; build final com Corretto 21 | — |
| Stack tecnológica | Web MVC, JPA, Validation, PostgreSQL e Flyway | ATENDE | Dependências do `pom.xml`; `application.properties` | — |
| Stack tecnológica | Testes JUnit 5, Mockito e MockMvc | ATENDE | Testes em `src/test/java`; starters de teste no `pom.xml` | — |
| Arquitetura | Controllers delegam para services e usam DTOs | ATENDE | `*Controller`, requests/responses e `ApiMapper` | Entidades não são expostas nas respostas HTTP. |
| Arquitetura | Regras e validações de domínio nos services | ATENDE | `ChamadoService`, `InteracaoService`, `UsuarioService`, `CategoriaService`, `SetorService` | Controllers apenas convertem/parâmetros e validam o contrato HTTP. |
| Arquitetura | Repositories Spring Data e filtros no banco | ATENDE | Repositories `JpaRepository`/`JpaSpecificationExecutor`; `repository/specification` | Não há carregamento integral para filtrar em memória. |
| Arquitetura | Injeção por construtor, sem `@Autowired` em atributos e sem ciclo necessário | ATENDE | Construtores explícitos; busca por `@Autowired` em `src/main` sem resultados | — |
| Entidades e relacionamentos | Setor: sigla única, categorias e técnicos | ATENDE | `Setor.java`; `V1__criar_tabelas.sql` (`uk_setor_sigla`) | Relações inversas `@OneToMany`. |
| Entidades e relacionamentos | Categoria: setor obrigatório | ATENDE | `Categoria.java`; FK `fk_categoria_setor` | `@ManyToOne(optional=false)`. |
| Entidades e relacionamentos | Usuário: e-mail único, tipo e setor de técnico | ATENDE | `Usuario.java`, `UsuarioService.preencherDadosValidos`, `uk_usuario_email` | Service exige setor para `TECNICO` e remove setor de solicitante. |
| Entidades e relacionamentos | Chamado: campos e três associações corretas | ATENDE | `Chamado.java`; tabela `chamado` em V1 | Solicitante/categoria obrigatórios; técnico opcional. |
| Entidades e relacionamentos | Interação: chamado, autor e data | ATENDE | `Interacao.java`; tabela `interacao` em V1 | — |
| Regras de negócio | Abertura inicial ABERTO, data automática e prioridade MEDIA | ATENDE | `ChamadoService.abrirChamado`; `ChamadoServiceTest.solicitanteAbreChamadoComEstadoInicialControladoPeloSistema` | — |
| Regras de negócio | Apenas solicitante abre chamado | ATENDE | `ChamadoService.abrirChamado`; `tecnicoNaoPodeAbrirChamado` | — |
| Regras de negócio | Assumir: aberto, técnico e mesmo setor | ATENDE | `ChamadoService.assumirChamado`; testes de setor e status | Confirmado também por HTTP real: técnico de outro setor retornou 409. |
| Regras de negócio | Resolver: técnico atribuído, solução e fechamento | ATENDE | `ChamadoService.resolverChamado`; `tecnicoResponsavelResolveChamado` | — |
| Regras de negócio | Cancelar: próprio solicitante e chamado aberto | ATENDE | `ChamadoService.cancelarChamado`; testes de cancelamento | — |
| Regras de negócio | Encerrados não aceitam interação ou transição | ATENDE | `InteracaoService.registrarInteracao`; `chamadoResolvidoNaoSofreNovasTransicoes` | — |
| Regras de negócio | Interação apenas por solicitante ou técnico atribuído | ATENDE | `InteracaoService.autorPermitido`; `InteracaoServiceTest` | — |
| Regras de negócio | Exclusão bloqueada para categoria/setor com chamado | ATENDE | `CategoriaService.excluir`, `SetorService.excluir`; `CadastroServiceTest` | Confirmado por HTTP real: ambos retornaram 409. |
| Regras de negócio | Ciclo ABERTO→EM_ATENDIMENTO→RESOLVIDO ou ABERTO→CANCELADO | ATENDE | Métodos de transição em `ChamadoService`; teste `chamadoResolvidoNaoSofreNovasTransicoes` | Transição inválida real retornou 409. |
| Endpoints | Seis rotas de setores | ATENDE | `SetorController`; validação HTTP real 201/200/200/200/204/200 | — |
| Endpoints | Cinco rotas de categorias | ATENDE | `CategoriaController`; validação HTTP real 201/200/200/200/204 | — |
| Endpoints | Cinco rotas de usuários | ATENDE | `UsuarioController`; validação HTTP real 201/200/200/200/200 | — |
| Endpoints | Sete rotas de chamados e interações | ATENDE | `ChamadoController`; validação HTTP real 201/200/200/200/200/200/201 | Detalhe retorna chamado e interações cronológicas. |
| Paginação e filtros | Defaults, limites e envelope de página | ATENDE | `WebSupport.page`, `PageResponse`; `*ControllerTest` | Default 0/10 e máximo 50. |
| Paginação e filtros | Filtros opcionais/combináveis em Specifications | ATENDE | `SetorSpecification`, `CategoriaSpecification`, `UsuarioSpecification`, `ChamadoSpecification` | Aplicados por `findAll(specification, pageable)`. |
| Paginação e filtros | Ordenação e parâmetros inválidos retornam 400 | ATENDE | `WebSupport`; `UsuarioControllerTest` e `ChamadoControllerTest` | Lista permitida por recurso impede campo inexistente. |
| Tratamento de erros | Formato JSON de erro obrigatório | ATENDE | `GlobalExceptionHandler` com `timestamp`, `status`, `mensagem`, `caminho` | `@RestControllerAdvice`. |
| Tratamento de erros | 400 para validação, JSON/enum/data/paginação inválidos | ATENDE | Handler e `ChamadoControllerTest` | Também confirmado por POST inválido real (400). |
| Tratamento de erros | 404, 409 e 500 sem dados internos | ATENDE | Handler e `ChamadoControllerTest.padronizaConflitoDeIntegridadeEErroInesperadoSemDetalhesInternos` | 404/409 confirmados por HTTP real. |
| PostgreSQL e Flyway | Configuração por variáveis, sem senha no properties | ATENDE | `application.properties`, `docker-compose.yml` | Usa `DB_URL`, `DB_USER` e `DB_PASSWORD`. |
| PostgreSQL e Flyway | V1 cria cinco tabelas, FKs, checks e índices | ATENDE | `V1__criar_tabelas.sql` | Migration não foi alterada. |
| PostgreSQL e Flyway | V2 contém dados mínimos | ATENDE | `V2__inserir_dados_iniciais.sql` | 2 setores, 4 categorias e 4 usuários. |
| PostgreSQL e Flyway | Histórico, checksum e `ddl-auto=validate` | ATENDE | Logs Flyway, `flyway_schema_history` (V1/V2 `success=true`), README e properties | Inicialização confirmou schema na versão 2. |
| PostgreSQL e Flyway | Enums persistidos como strings | ATENDE | `@Enumerated(EnumType.STRING)` e checks SQL | — |
| Docker | Dockerfile multi-stage com Maven e JRE 21 | ATENDE | `sistema-chamados/Dockerfile` | Imagem final `eclipse-temurin:21-jre`. |
| Docker | Compose: PostgreSQL 17, healthcheck, volume e dependência | ATENDE | `docker-compose.yml` | API depende do banco saudável. |
| Docker | Configuração padrão porta 8080 | ATENDE | `docker compose config` mostrou `published: "8080"` | Sem porta externa ocupada durante a validação. |
| Docker | Inicialização real e Flyway/Hibernate | ATENDE | `docker compose -p auditoria-final up -d --build`; logs da API | API respondeu `GET /setores` em `http://localhost:8080`; PostgreSQL 17.11 e Flyway validaram V1/V2. |
| Testes | Serviços, regras e transições | ATENDE | `ChamadoServiceTest` (10), `CadastroServiceTest` (3), `InteracaoServiceTest` (4) | — |
| Testes | Controllers, DTOs, filtros, página e erros HTTP | ATENDE | Quatro `*ControllerTest` com MockMvc (14 testes) | Inclui contrato de respostas e erros 400/404/409/500. |
| Testes | Execução final completa | ATENDE | `./mvnw clean test`: 32 testes, 0 falhas, 0 erros, 0 ignorados | Executado com PostgreSQL 17 isolado. |
| README e Postman | README de clone, Docker, testes, endpoints e filtros | ATENDE | `README.md` revisado nesta auditoria | Corrigida referência obsoleta de ausência de controllers. |
| README e Postman | Explicação obrigatória de Flyway | ATENDE | Seção “Persistência e migrations” do README | Cobre histórico, checksum, `update` e `validate`. |
| README e Postman | Coleção Postman v2.1, variáveis e 23 rotas | ATENDE | `postman/Sistema_Chamados_IFRN.postman_collection.json`; `jq empty` | IDs fixos iniciais foram removidos; scripts capturam IDs de criação. |
| README e Postman | Exemplos de filtros, paginação e erros na coleção | ATENDE | Pastas “Filtros e paginação” e “Testes de erros” | Sem credenciais reais. |
| GitHub e entrega | Main sincronizada e branch de auditoria criada | ATENDE | `git fetch origin`, `main == origin/main`, branch `chore/auditoria-final-entrega` | Alterações locais preexistentes em `.idea` foram preservadas. |
| GitHub e entrega | Repositório público | ATENDE | Página pública do repositório no GitHub exibiu o selo “Public” | Verificado em 30/10/2026. |
| GitHub e entrega | PDF do professor como fonte primária | NÃO VERIFICADO | Busca no workspace não localizou `Projeto_API_Sistema_de_Chamados_IFRN.pdf` | Auditoria baseada na especificação detalhada recebida. |
| GitHub e entrega | Entrega no Google Sala de Aula | NÃO VERIFICADO | A plataforma não está no escopo nem conectada ao workspace | Ação manual restante após o push. |

## Resultado

- Total de requisitos auditados: **51**.
- Atende: **49**.
- Parcial: **0**.
- Não atende: **0**.
- Não verificado: **2**.

Não há pendência técnica obrigatória identificada. As únicas verificações pendentes são a comparação direta com o PDF indisponível e a entrega manual no Google Sala de Aula. A auditoria corrigiu a documentação obsoleta do README e removeu IDs de recurso predefinidos da coleção Postman.
