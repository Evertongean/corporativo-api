INSERT INTO setor (nome, sigla) VALUES
    ('Coordenação de Tecnologia da Informação e Comunicação', 'COTIC'),
    ('Setor de Manutenção', 'MANUT');

INSERT INTO categoria (nome, descricao, setor_id)
SELECT dados.nome, dados.descricao, setor.id
FROM (
    VALUES
        ('Computadores', 'Suporte para computadores e periféricos.', 'COTIC'),
        ('Rede e Internet', 'Suporte para rede e acesso à internet.', 'COTIC'),
        ('Instalações Elétricas', 'Solicitações relacionadas a instalações elétricas.', 'MANUT'),
        ('Infraestrutura', 'Solicitações relacionadas à infraestrutura física.', 'MANUT')
) AS dados(nome, descricao, setor_sigla)
JOIN setor ON setor.sigla = dados.setor_sigla;

INSERT INTO usuario (nome, email, matricula, tipo, setor_id) VALUES
    ('Solicitante Um', 'solicitante.um@example.com', '20260001', 'SOLICITANTE', NULL),
    ('Solicitante Dois', 'solicitante.dois@example.com', '20260002', 'SOLICITANTE', NULL);

INSERT INTO usuario (nome, email, matricula, tipo, setor_id)
SELECT dados.nome, dados.email, dados.matricula, 'TECNICO', setor.id
FROM (
    VALUES
        ('Técnico COTIC', 'tecnico.cotic@example.com', '20260003', 'COTIC'),
        ('Técnico MANUT', 'tecnico.manut@example.com', '20260004', 'MANUT')
) AS dados(nome, email, matricula, setor_sigla)
JOIN setor ON setor.sigla = dados.setor_sigla;
