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
