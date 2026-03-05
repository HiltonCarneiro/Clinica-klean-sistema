package br.com.clinica.auth;

public enum Permissao {
    // Paciente
    PACIENTE_VER("VER PACIENTE"),
    PACIENTE_CRIAR ("CRIAR PACIENTE"),
    PACIENTE_EDITAR("EDITAR PACIENTE"),
    PACIENTE_ATIVAR_INATIVAR("ATIVAR / INATIVAR PACIENTE"),

    // Estoque
    ESTOQUE_VER("VER ESTOQUE"),
    ESTOQUE_CADASTRAR_EDITAR("CADASTRAR / EDITAR ITEM NO ESTOQUE"),
    ESTOQUE_ATIVAR_INATIVAR("ATIVAR / INATIVAR ITEM NO ESTOQUE"),
    ESTOQUE_VER_PRECO_CUSTO("VER PREÇO DE CUSTO NO ESTOQUE"), // opcional (só admin)

    // Usuários
    USUARIO_GERENCIAR("GERENCIAR USUÁRIO"),

    // Auditoria (somente admin)
    AUDITORIA_VER("VER AUDITORIA"),

    // Agenda
    AGENDA_VER("VER AGENDA"),
    AGENDA_GERENCIAR("GERENCIAR AGENDA"),

    // Financeiro
    FINANCEIRO_VER("VER FINANCEIRO"),
    NOTA_GERAR("GERAR NOTA"),
    RELATORIOS_VER("VER RELATÓRIOS"),

    // Prontuário
    PRONTUARIO_VER("VER PRONTUÁRIOS"),
    PRONTUARIO_CRIAR("CRIAR PRONTUÁRIO"),
    PRONTUARIO_EDITAR_PROPRIO("EDITAR PRÓPRIO PRONTUÁRIO");

    private final String label;

    Permissao(String label) { this.label = label; }

    public String getLabel() { return label; }
}
