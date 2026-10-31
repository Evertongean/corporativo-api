# corporativo-api

## Executar com Docker Compose

Com Docker e Docker Compose instalados, inicie a API e o PostgreSQL a partir da raiz do repositório:

```bash
docker compose up
```

O Compose inicia os serviços `db` (PostgreSQL 17) e `api` (Spring Boot). A API fica disponível na porta `8080` do host. Nesta etapa ainda não há controllers; portanto, uma resposta `404` em `http://localhost:8080/` confirma que a aplicação Spring Boot está respondendo, mas não que exista uma rota.

O banco não publica a porta `5432` no host, prevenindo conflito com uma instalação local. Seus dados ficam persistidos no volume nomeado `postgres_data`; para parar os serviços sem apagar dados, use:

```bash
docker compose down
```

Para acompanhar a inicialização ou investigar falhas, consulte os logs:

```bash
docker compose logs -f api
docker compose logs -f db
```

### Variáveis de ambiente

O Compose possui valores padrão exclusivos para desenvolvimento local, para que `docker compose up` funcione em um clone novo. Eles não são credenciais de produção:

- `DB_URL`: por padrão, `jdbc:postgresql://db:5432/chamados`.
- `DB_USER`: por padrão, `chamados`.
- `DB_PASSWORD`: por padrão, `chamados_dev_password`; é usado tanto pela API quanto pelo PostgreSQL.
- `API_PORT`: por padrão, `8080`; altere-o apenas se essa porta já estiver ocupada no host.

Essas variáveis podem ser sobrescritas no ambiente antes de executar o Compose. Ao alterar o banco na URL, mantenha-o compatível com o banco `chamados` criado pelo serviço `db`. Nunca versione um arquivo `.env` com credenciais reais. O arquivo [`sistema-chamados/.env.example`](sistema-chamados/.env.example) contém valores de referência para execução local fora do Docker.

## Persistência e migrations

A aplicação `sistema-chamados` utiliza PostgreSQL. A conexão é configurada por `DB_URL`, `DB_USER` e `DB_PASSWORD`, sem senhas fixas em `application.properties`.

O Flyway executa, em ordem, os scripts em `sistema-chamados/src/main/resources/db/migration`. Ele registra cada versão aplicada e seu checksum na tabela `flyway_schema_history`. Uma migration já aplicada não deve ser modificada: se seu conteúdo mudar, o checksum calculado deixa de corresponder ao registrado e o Flyway interrompe a validação, protegendo a consistência do histórico. Para evoluir o banco, deve-se adicionar uma nova migration versionada.

O Hibernate está configurado com `spring.jpa.hibernate.ddl-auto=validate` e `spring.jpa.open-in-view=false`. Assim, ele apenas verifica se o mapeamento JPA corresponde ao esquema criado pelo Flyway; não cria nem altera tabelas. Isso evita alterações implícitas no banco e mantém a evolução do schema versionada e reprodutível, ao contrário de `ddl-auto=update`.

## API REST

As rotas públicas usam JSON, paginação (`page`, `size`, `sort`) e retornam dados por DTO. O tamanho padrão é 10 e o máximo é 50. Recursos disponíveis: `/setores`, `/categorias`, `/usuarios` e `/chamados`.

```bash
curl -X POST localhost:8080/setores -H 'Content-Type: application/json' -d '{"nome":"Tecnologia","sigla":"TI"}'
curl -X POST localhost:8080/categorias -H 'Content-Type: application/json' -d '{"nome":"Rede","descricao":"Internet","setorId":1}'
curl -X POST localhost:8080/usuarios -H 'Content-Type: application/json' -d '{"nome":"Aluno","email":"aluno@example.com","matricula":"20260005","tipo":"SOLICITANTE"}'
curl -X POST localhost:8080/chamados -H 'Content-Type: application/json' -d '{"titulo":"Sem rede","descricao":"Internet indisponível","local":"Sala 1","prioridade":"ALTA","solicitanteId":1,"categoriaId":1}'
curl -X PATCH localhost:8080/chamados/1/assumir -H 'Content-Type: application/json' -d '{"tecnicoId":3}'
curl -X PATCH localhost:8080/chamados/1/resolver -H 'Content-Type: application/json' -d '{"tecnicoId":3,"solucao":"Equipamento ajustado"}'
curl -X PATCH localhost:8080/chamados/1/cancelar -H 'Content-Type: application/json' -d '{"solicitanteId":1}'
curl -X POST localhost:8080/chamados/1/interacoes -H 'Content-Type: application/json' -d '{"autorId":1,"texto":"Ainda ocorre"}'
curl 'localhost:8080/chamados?status=ABERTO&prioridade=ALTA&page=0&size=10&sort=dataAbertura,desc'
```

Os erros usam o formato `{ "timestamp", "status", "mensagem", "caminho" }`; por exemplo, dados inválidos retornam 400, recurso inexistente 404 e violações de regra 409.

## Testes automatizados

Use Java 21 e execute os comandos dentro de `sistema-chamados`:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto ./mvnw clean test
JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto ./mvnw clean package
```

Além dos testes de serviço, há testes REST com MockMvc para `SetorController`, `CategoriaController`, `UsuarioController` e `ChamadoController`. Eles simulam os serviços, portanto não dependem de PostgreSQL. Os testes verificam os contratos HTTP, validação, paginação, filtros, serialização e o formato padronizado de erros.

Para uma execução completa que também carrega o contexto da aplicação, informe um PostgreSQL disponível pelas variáveis `DB_URL`, `DB_USER` e `DB_PASSWORD` ou utilize o Compose descrito abaixo.

## Execução com Docker e validação manual

Da raiz do repositório, inicie a aplicação e o PostgreSQL 17:

```bash
docker compose up --build
```

Se a porta 8080 estiver ocupada, escolha outra sem interromper serviços existentes:

```bash
API_PORT=8081 docker compose up --build
```

Os dados iniciais são criados pelo Flyway: dois setores, quatro categorias, dois solicitantes e dois técnicos. Consulte `GET /setores`, `GET /categorias` e `GET /usuarios` para obter os IDs reais antes de fazer operações de escrita.

Exemplos de filtros:

```text
GET /chamados?status=ABERTO&prioridade=ALTA&setorId=1&page=0&size=10&sort=dataAbertura,desc
GET /categorias?nome=Rede&setorId=1&page=0&size=10
GET /usuarios?tipo=TECNICO&setorId=1&sort=nome,asc
```

`page` começa em 0, `size` é 10 por padrão e aceita no máximo 50. A resposta sempre contém `content`, `page`, `size`, `totalElements` e `totalPages`. Campos de ordenação inexistentes, datas inválidas, páginas negativas e tamanhos inválidos retornam 400.

Os códigos mais frequentes são: 400 para parâmetros ou corpo inválidos; 404 para recurso inexistente; e 409 para e-mail/sigla duplicados, integridade, permissões e transições de chamado não permitidas. O corpo do erro inclui `timestamp`, `status`, `mensagem` e `caminho`.

## Postman

Importe [`postman/Sistema_Chamados_IFRN.postman_collection.json`](postman/Sistema_Chamados_IFRN.postman_collection.json) no Postman. A variável `baseUrl` inicia em `http://localhost:8080`; altere-a para `http://localhost:8081` se tiver usado `API_PORT=8081`.

Execute a coleção nesta sequência para aproveitar a captura automática de IDs: crie setor, categoria, solicitante e técnico; abra o chamado; registre uma interação; assuma e resolva o chamado. Para testar cancelamento, abra um novo chamado e cancele-o antes de assumir. Execute as requisições de exclusão apenas ao final, pois removem os recursos criados. As pastas de filtros e erros contêm exemplos positivos e negativos, sem credenciais ou IDs fixos.
