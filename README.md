# corporativo-api

## Persistência e migrations

A aplicação `sistema-chamados` utiliza PostgreSQL. A conexão é configurada por variáveis de ambiente, para que credenciais não sejam incluídas no código:

- `DB_URL`: URL JDBC, por exemplo `jdbc:postgresql://localhost:5432/sistema_chamados`.
- `DB_USER`: usuário do PostgreSQL.
- `DB_PASSWORD`: senha do usuário do PostgreSQL.

O arquivo [`sistema-chamados/.env.example`](sistema-chamados/.env.example) contém valores de referência. Arquivos `.env` são ignorados pelo Git e não devem ser versionados.

O Flyway executa, em ordem, os scripts em `sistema-chamados/src/main/resources/db/migration`. Ele registra cada versão aplicada e seu checksum na tabela `flyway_schema_history`. Uma migration já aplicada não deve ser modificada: se seu conteúdo mudar, o checksum calculado deixa de corresponder ao registrado e o Flyway interrompe a validação, protegendo a consistência do histórico. Para evoluir o banco, deve-se adicionar uma nova migration versionada.

O Hibernate está configurado com `spring.jpa.hibernate.ddl-auto=validate`. Assim, ele apenas verifica se o mapeamento JPA futuro corresponde ao esquema criado pelo Flyway; não cria nem altera tabelas. Isso evita alterações implícitas no banco e mantém a evolução do schema versionada e reprodutível, ao contrário de `ddl-auto=update`.
