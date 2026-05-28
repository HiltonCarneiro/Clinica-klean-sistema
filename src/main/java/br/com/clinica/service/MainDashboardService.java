package br.com.clinica.service;

import br.com.clinica.auth.AuthGuard;
import br.com.clinica.auth.Permissao;
import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.AuditoriaDAO;
import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.ProdutoDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Produto;
import br.com.clinica.model.enums.TipoMovimento;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

public class MainDashboardService {

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();
    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();
    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    public DashboardResumo carregarResumo() {
        return new DashboardResumo(
                carregarAgendaHoje(),
                carregarPacientesAtivos(),
                temPermissao(Permissao.ESTOQUE_VER) ? carregarEstoqueCritico() : estoqueRestrito(),
                temPermissao(Permissao.FINANCEIRO_VER) ? carregarFinanceiroHoje() : financeiroRestrito(),
                temPermissao(Permissao.AUDITORIA_VER) ? carregarAuditoriaRecente() : ""
        );
    }

    private IndicadorResumo carregarAgendaHoje() {
        try {
            List<Agendamento> agendaHoje = agendamentoDAO.listarPorData(LocalDate.now());
            boolean vazia = agendaHoje.isEmpty();

            return new IndicadorResumo(
                    agendaHoje.size() + " agendamento(s)",
                    vazia ? "Nenhum atendimento pendente hoje" : "Próximo: " + safe(agendaHoje.get(0).getPacienteNome()),
                    vazia ? "• Hoje não há agendamentos pendentes." : "• Existem " + agendaHoje.size() + " atendimento(s) pendente(s) hoje."
            );
        } catch (Exception e) {
            return new IndicadorResumo("-", "Não foi possível carregar a agenda", "• Agenda indisponível no momento.");
        }
    }

    private IndicadorResumo carregarPacientesAtivos() {
        try {
            int ativos = pacienteDAO.listarTodos(false).size();

            return new IndicadorResumo(
                    ativos + " ativo(s)",
                    "Pacientes disponíveis para atendimento",
                    "• " + ativos + " paciente(s) ativo(s) cadastrados."
            );
        } catch (Exception e) {
            return new IndicadorResumo("-", "Não foi possível carregar pacientes", "• Pacientes indisponíveis no momento.");
        }
    }

    private IndicadorResumo carregarEstoqueCritico() {
        try {
            List<Produto> baixoEstoque = produtoDAO.listar(false, true, false);
            boolean vazio = baixoEstoque.isEmpty();

            return new IndicadorResumo(
                    baixoEstoque.size() + " item(ns)",
                    vazio ? "Nenhum item crítico" : "Itens abaixo do estoque mínimo",
                    vazio ? "• Estoque sem alertas críticos." : "• " + baixoEstoque.size() + " item(ns) precisam de atenção no estoque."
            );
        } catch (Exception e) {
            return new IndicadorResumo("-", "Não foi possível carregar estoque", "• Estoque indisponível no momento.");
        }
    }

    private IndicadorResumo carregarFinanceiroHoje() {
        try {
            List<MovimentoCaixa> movimentos = movimentoCaixaDAO.listarPorPeriodo(LocalDate.now(), LocalDate.now());

            double entradas = movimentos.stream()
                    .filter(m -> m.getTipo() == TipoMovimento.ENTRADA)
                    .mapToDouble(MovimentoCaixa::getValor)
                    .sum();

            double saidas = movimentos.stream()
                    .filter(m -> m.getTipo() == TipoMovimento.SAIDA)
                    .mapToDouble(MovimentoCaixa::getValor)
                    .sum();

            double saldo = entradas - saidas;

            return new IndicadorResumo(
                    formatMoney(saldo),
                    movimentos.size() + " movimentação(ões) hoje",
                    "• Saldo do dia: " + formatMoney(saldo)
                            + " | Entradas: " + formatMoney(entradas)
                            + " | Saídas: " + formatMoney(saidas)
            );
        } catch (Exception e) {
            return new IndicadorResumo("-", "Não foi possível carregar financeiro", "• Financeiro indisponível no momento.");
        }
    }

    private String carregarAuditoriaRecente() {
        try {
            List<AuditoriaDAO.LinhaAuditoria> ultimos = auditoriaDAO.listarUltimos(6);

            if (ultimos.isEmpty()) {
                return "• Nenhuma atividade recente registrada.";
            }

            StringBuilder sb = new StringBuilder();

            for (AuditoriaDAO.LinhaAuditoria linha : ultimos) {
                sb.append("• ")
                        .append(formatarAcaoAuditoria(linha.acao))
                        .append(" ")
                        .append(formatarEntidadeAuditoria(linha.entidade))
                        .append("\n")
                        .append("  Usuário: ")
                        .append(formatarUsuarioAuditoria(linha.usuario))
                        .append("\n\n");
            }

            return sb.toString().trim();
        } catch (Exception e) {
            return "• Auditoria indisponível no momento.";
        }
    }

    private IndicadorResumo estoqueRestrito() {
        return new IndicadorResumo("-", "Acesso restrito", "• Consulte o histórico clínico antes de iniciar um atendimento.");
    }

    private IndicadorResumo financeiroRestrito() {
        return new IndicadorResumo("-", "Acesso restrito", "• Registre evoluções e prontuários conforme o atendimento.");
    }

    private boolean temPermissao(Permissao permissao) {
        try {
            AuthGuard.exigirPermissao(permissao);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String formatMoney(double valor) {
        return NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
    }

    private String formatarAcaoAuditoria(String acao) {
        if (acao == null || acao.isBlank()) {
            return "Atividade registrada";
        }

        return switch (acao.toUpperCase()) {
            case "LOGIN_OK" -> "Login realizado";
            case "LOGOUT" -> "Sessão encerrada";
            case "CRIAR", "CREATE", "INSERT" -> "Cadastro realizado";
            case "EDITAR", "UPDATE" -> "Registro atualizado";
            case "EXCLUIR", "DELETE" -> "Registro removido";
            default -> acao.replace("_", " ");
        };
    }

    private String formatarEntidadeAuditoria(String entidade) {
        if (entidade == null || entidade.isBlank()) {
            return "";
        }

        return switch (entidade.toUpperCase()) {
            case "USUARIO" -> "em usuários";
            case "PACIENTE" -> "em pacientes";
            case "AGENDAMENTO" -> "na agenda";
            case "CAIXA", "MOVIMENTO_CAIXA" -> "no financeiro";
            case "PRODUTO", "ESTOQUE" -> "no estoque";
            default -> "em " + entidade.toLowerCase();
        };
    }

    private String formatarUsuarioAuditoria(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            return "Não identificado";
        }

        if (usuario.contains("(")) {
            return usuario.substring(0, usuario.indexOf("(")).trim();
        }

        return usuario;
    }

    public record DashboardResumo(
            IndicadorResumo agenda,
            IndicadorResumo pacientes,
            IndicadorResumo estoque,
            IndicadorResumo financeiro,
            String auditoriaRecente
    ) {}

    public record IndicadorResumo(String valor, String subtitulo, String resumo) {}
}