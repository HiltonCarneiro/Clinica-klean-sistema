package br.com.clinica.auth;

public enum Permissao {
    // Paciente
    PACIENTE_VER,
    PACIENTE_CRIAR,
    PACIENTE_EDITAR,
    PACIENTE_ATIVAR_INATIVAR,

    // Estoque
    ESTOQUE_VER,
    ESTOQUE_CADASTRAR_EDITAR,
    ESTOQUE_ATIVAR_INATIVAR,
    ESTOQUE_VER_PRECO_CUSTO, // opcional (só admin)

    // Usuários
    USUARIO_GERENCIAR,

    // Auditoria (somente admin)
    AUDITORIA_VER,

    // Agenda
    AGENDA_VER,
    AGENDA_GERENCIAR,

    // Financeiro
    FINANCEIRO_VER,
    NOTA_GERAR,
    RELATORIOS_VER,

    // Prontuário
    PRONTUARIO_VER,
    PRONTUARIO_CRIAR,
    PRONTUARIO_EDITAR_PROPRIO
}
