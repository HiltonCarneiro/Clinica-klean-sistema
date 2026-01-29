package br.com.clinica.auth;

import java.util.*;

public final class Policy {
    private Policy() {}

    private static final Map<String, Set<Permissao>> REGRAS = new HashMap<>();

    static {
        // =========================
        // RECEPCIONISTA
        // =========================
        // Pode: estoque (ver/editar/ativar), agenda (ver/gerenciar), usuários (gerenciar), caixa (ver/gerar nota)
        REGRAS.put(Perfis.RECEPCIONISTA, EnumSet.of(
                // Estoque
                Permissao.ESTOQUE_VER,
                Permissao.ESTOQUE_CADASTRAR_EDITAR,
                Permissao.ESTOQUE_ATIVAR_INATIVAR,

                // Agenda
                Permissao.AGENDA_VER,
                Permissao.AGENDA_GERENCIAR,

                // Usuários
                Permissao.USUARIO_GERENCIAR,

                //Pacientes
                Permissao.PACIENTE_CRIAR,
                Permissao.PACIENTE_EDITAR,
                Permissao.PACIENTE_VER,
                Permissao.PACIENTE_ATIVAR_INATIVAR,

                // Caixa / Financeiro
                Permissao.FINANCEIRO_VER,
                Permissao.NOTA_GERAR
        ));

        // =========================
        // PROFISSIONAIS DA SAÚDE
        // =========================
        // Podem: agenda ver/gerenciar, cadastrar/editar paciente, prontuário ver/criar/editar próprio
        Set<Permissao> PROFISSIONAL_SAUDE = EnumSet.of(
                // Agenda
                Permissao.AGENDA_VER,
                Permissao.AGENDA_GERENCIAR,

                // Pacientes (cadastro)
                Permissao.PACIENTE_VER,
                Permissao.PACIENTE_CRIAR,
                Permissao.PACIENTE_EDITAR,

                // Pacientes (ficha)
                Permissao.RELATORIOS_VER,

                // Prontuário (futuro)
                Permissao.PRONTUARIO_VER,
                Permissao.PRONTUARIO_CRIAR,
                Permissao.PRONTUARIO_EDITAR_PROPRIO
        );

        REGRAS.put(Perfis.ENFERMEIRA, EnumSet.copyOf(PROFISSIONAL_SAUDE));
        REGRAS.put(Perfis.NUTRICIONISTA, EnumSet.copyOf(PROFISSIONAL_SAUDE));
        REGRAS.put(Perfis.FISIOTERAPEUTA, EnumSet.copyOf(PROFISSIONAL_SAUDE));
        REGRAS.put(Perfis.PSICOLOGA, EnumSet.copyOf(PROFISSIONAL_SAUDE));
        REGRAS.put(Perfis.MEDICA, EnumSet.copyOf(PROFISSIONAL_SAUDE));

        // =========================
        // ADMINISTRADOR
        // =========================
        // Acesso total: todas as permissões existentes
        REGRAS.put(Perfis.ADMIN, EnumSet.allOf(Permissao.class));
    }

    public static boolean temPermissao(String nomePerfil, Permissao permissao) {
        if (nomePerfil == null || permissao == null) return false;
        String key = nomePerfil.trim().toUpperCase();
        Set<Permissao> set = REGRAS.get(key);
        return set != null && set.contains(permissao);
    }
}
