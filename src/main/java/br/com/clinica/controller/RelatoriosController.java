package br.com.clinica.controller;

import br.com.clinica.dao.AgendamentoDAO;
import br.com.clinica.dao.MovimentoCaixaDAO;
import br.com.clinica.dao.NotaDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.MovimentoCaixa;
import br.com.clinica.model.Nota;
import br.com.clinica.model.enums.TipoMovimento;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.NotaPdfService;
import br.com.clinica.service.RelatorioAgendamentosPdfService;
import br.com.clinica.service.RelatorioCaixaPdfService;
import br.com.clinica.service.RelatorioNotasPdfService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class RelatoriosController {

    // ABA CAIXA
    @FXML private DatePicker dtCxInicio;
    @FXML private DatePicker dtCxFim;

    @FXML private TableView<MovimentoCaixa> tblCaixa;
    @FXML private TableColumn<MovimentoCaixa, String> colCxData;
    @FXML private TableColumn<MovimentoCaixa, String> colCxTipo;
    @FXML private TableColumn<MovimentoCaixa, String> colCxDescricao;
    @FXML private TableColumn<MovimentoCaixa, String> colCxForma;
    @FXML private TableColumn<MovimentoCaixa, Double> colCxValor;
    @FXML private TableColumn<MovimentoCaixa, String> colCxPaciente;

    @FXML private Label lblCxEntradas;
    @FXML private Label lblCxSaidas;
    @FXML private Label lblCxSaldo;

    private final ObservableList<MovimentoCaixa> caixaObs = FXCollections.observableArrayList();

    // ABA AGENDAMENTOS
    @FXML private DatePicker dtAgInicio;
    @FXML private DatePicker dtAgFim;
    @FXML private ComboBox<Usuario> cbAgProfissional;

    @FXML private TableView<Agendamento> tblAgendamentos;
    @FXML private TableColumn<Agendamento, String> colAgData;
    @FXML private TableColumn<Agendamento, String> colAgHora;
    @FXML private TableColumn<Agendamento, String> colAgProf;
    @FXML private TableColumn<Agendamento, String> colAgSala;
    @FXML private TableColumn<Agendamento, String> colAgPaciente;
    @FXML private TableColumn<Agendamento, String> colAgStatus;
    @FXML private TableColumn<Agendamento, String> colAgProced;

    private final ObservableList<Agendamento> agObs = FXCollections.observableArrayList();

    // ABA NOTAS
    @FXML private DatePicker dtNotaInicio;
    @FXML private DatePicker dtNotaFim;
    @FXML private TextField txtPacienteFiltro;
    @FXML private ComboBox<Usuario> cbNotaProfissional;
    @FXML private ComboBox<String> cbNotaForma;

    @FXML private TableView<NotaDAO.NotaResumo> tblNotas;
    @FXML private TableColumn<NotaDAO.NotaResumo, Long> colNotaId;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaDataHora;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaPaciente;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaProfissional;
    @FXML private TableColumn<NotaDAO.NotaResumo, String> colNotaForma;
    @FXML private TableColumn<NotaDAO.NotaResumo, Double> colNotaTotal;

    private final ObservableList<NotaDAO.NotaResumo> notasObs = FXCollections.observableArrayList();

    // DAOs / Services
    private final MovimentoCaixaDAO movimentoCaixaDAO = new MovimentoCaixaDAO();
    private final AgendamentoDAO agendamentoDAO = new AgendamentoDAO();
    private final NotaDAO notaDAO = new NotaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final NotaPdfService notaPdfService = new NotaPdfService();
    private final RelatorioCaixaPdfService relCaixaPdf = new RelatorioCaixaPdfService();
    private final RelatorioAgendamentosPdfService relAgPdf = new RelatorioAgendamentosPdfService();
    private final RelatorioNotasPdfService relNotasPdf = new RelatorioNotasPdfService();

    private static final String FORMA_TODAS = "TODAS";

    @FXML
    public void initialize() {
        LocalDate hoje = LocalDate.now();

        // Datas padrão
        dtCxInicio.setValue(hoje);
        dtCxFim.setValue(hoje);
        dtAgInicio.setValue(hoje);
        dtAgFim.setValue(hoje);
        dtNotaInicio.setValue(hoje);
        dtNotaFim.setValue(hoje);

        // Profissionais
        List<Usuario> profissionais = usuarioDAO.listarProfissionaisAtivos();
        ObservableList<Usuario> profObs = FXCollections.observableArrayList();
        profObs.add(null); // Todos
        profObs.addAll(profissionais);

        cbAgProfissional.setItems(profObs);
        configurarComboProfissionais(cbAgProfissional);
        cbAgProfissional.getSelectionModel().selectFirst();

        cbNotaProfissional.setItems(profObs);
        configurarComboProfissionais(cbNotaProfissional);
        cbNotaProfissional.getSelectionModel().selectFirst();

        // Formas
        cbNotaForma.setItems(FXCollections.observableArrayList(
                FORMA_TODAS, "DINHEIRO", "PIX", "CARTAO", "TRANSFERENCIA", "OUTRO"
        ));
        cbNotaForma.getSelectionModel().select(FORMA_TODAS);

        // Caixa
        tblCaixa.setItems(caixaObs);
        colCxData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData() != null ? c.getValue().getData().toString() : ""));
        colCxTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo() != null ? c.getValue().getTipo().name() : ""));
        colCxDescricao.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getDescricao())));
        colCxForma.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getFormaPagamento())));
        colCxValor.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getValor()).asObject());
        colCxPaciente.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getPacienteNome())));

        // Agendamentos
        tblAgendamentos.setItems(agObs);
        colAgData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData() != null ? c.getValue().getData().toString() : ""));
        colAgHora.setCellValueFactory(c -> new SimpleStringProperty(
                (c.getValue().getHoraInicio() != null ? c.getValue().getHoraInicio().toString() : "") +
                        " - " +
                        (c.getValue().getHoraFim() != null ? c.getValue().getHoraFim().toString() : "")
        ));
        colAgProf.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getProfissionalNome())));
        colAgSala.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSala() != null ? c.getValue().getSala().getDescricao() : ""));
        colAgPaciente.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getPacienteNome())));
        colAgStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus() != null ? c.getValue().getStatus().name() : ""));
        colAgProced.setCellValueFactory(c -> new SimpleStringProperty(nvl(c.getValue().getProcedimento())));

        // Notas
        tblNotas.setItems(notasObs);
        colNotaId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getId()).asObject());
        colNotaDataHora.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataHoraFmt()));
        colNotaPaciente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPacienteNome()));
        colNotaProfissional.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProfissionalNome()));
        colNotaForma.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormaPagamento()));
        colNotaTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotalLiquido()).asObject());

        // Inicial
        onBuscarCaixa();
        onBuscarAgendamentos();
        onBuscarNotas();
    }

    private void configurarComboProfissionais(ComboBox<Usuario> cb) {
        cb.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else if (item == null) setText("Todos");
                else setText(nomeProf(item));
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setText(null);
                else if (item == null) setText("Todos");
                else setText(nomeProf(item));
            }
        });
    }

    private String nomeProf(Usuario u) {
        if (u == null) return "";
        String pessoa = (u.getPessoaNome() != null && !u.getPessoaNome().isBlank()) ? u.getPessoaNome() : "";
        String cargo  = (u.getNome() != null && !u.getNome().isBlank()) ? u.getNome() : "";
        if (!pessoa.isBlank() && !cargo.isBlank()) return pessoa + " (" + cargo + ")";
        if (!pessoa.isBlank()) return pessoa;
        return cargo;
    }

    // CAIXA

    @FXML
    private void onBuscarCaixa() {
        try {
            LocalDate ini = dtCxInicio.getValue();
            LocalDate fim = dtCxFim.getValue();
            if (ini == null || fim == null) { erro("Período inválido", "Informe data inicial e final."); return; }
            if (fim.isBefore(ini)) { erro("Período inválido", "A data final não pode ser menor que a inicial."); return; }

            List<MovimentoCaixa> lista = movimentoCaixaDAO.listarPorPeriodo(ini, fim);
            caixaObs.setAll(lista);

            double entradas = lista.stream().filter(m -> m.getTipo() == TipoMovimento.ENTRADA).mapToDouble(MovimentoCaixa::getValor).sum();
            double saidas   = lista.stream().filter(m -> m.getTipo() == TipoMovimento.SAIDA).mapToDouble(MovimentoCaixa::getValor).sum();
            double saldo = entradas - saidas;

            lblCxEntradas.setText(String.format("R$ %.2f", entradas));
            lblCxSaidas.setText(String.format("R$ %.2f", saidas));
            lblCxSaldo.setText(String.format("R$ %.2f", saldo));

        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao buscar caixa", e.getMessage());
        }
    }

    @FXML
    private void onExportarCaixaPdf() {
        if (caixaObs.isEmpty()) { aviso("Nada para exportar", "Busque um relatório antes de exportar."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar relatório do caixa (PDF)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("relatorio_caixa.pdf");

        File destino = fc.showSaveDialog(tblCaixa.getScene().getWindow());
        if (destino == null) return;

        try {
            relCaixaPdf.gerar(dtCxInicio.getValue(), dtCxFim.getValue(), caixaObs, destino);
            aviso("Concluído", "PDF gerado em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao gerar PDF", e.getMessage());
        }
    }

    // AGENDAMENTOS

    @FXML
    private void onBuscarAgendamentos() {
        try {
            LocalDate ini = dtAgInicio.getValue();
            LocalDate fim = dtAgFim.getValue();
            if (ini == null || fim == null) { erro("Período inválido", "Informe data inicial e final."); return; }
            if (fim.isBefore(ini)) { erro("Período inválido", "A data final não pode ser menor que a inicial."); return; }

            Usuario prof = cbAgProfissional.getValue();

            List<Agendamento> lista = (prof == null)
                    ? agendamentoDAO.listarPorPeriodo(ini, fim)
                    : agendamentoDAO.listarPorPeriodoEProfissional(ini, fim, prof.getId());

            agObs.setAll(lista);

        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao buscar agendamentos", e.getMessage());
        }
    }

    @FXML
    private void onExportarAgendamentosPdf() {
        if (agObs.isEmpty()) { aviso("Nada para exportar", "Busque um relatório antes de exportar."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar relatório de agendamentos (PDF)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("relatorio_agendamentos.pdf");

        File destino = fc.showSaveDialog(tblAgendamentos.getScene().getWindow());
        if (destino == null) return;

        try {
            Usuario prof = cbAgProfissional.getValue();
            String profTxt = (prof == null) ? "" : nomeProf(prof);

            relAgPdf.gerar(dtAgInicio.getValue(), dtAgFim.getValue(), profTxt, agObs, destino);
            aviso("Concluído", "PDF gerado em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao gerar PDF", e.getMessage());
        }
    }

    // NOTAS

    @FXML
    private void onBuscarNotas() {
        try {
            LocalDate ini = dtNotaInicio.getValue();
            LocalDate fim = dtNotaFim.getValue();
            if (ini == null || fim == null) { erro("Período inválido", "Informe data inicial e final."); return; }
            if (fim.isBefore(ini)) { erro("Período inválido", "A data final não pode ser menor que a inicial."); return; }

            String pacienteLike = (txtPacienteFiltro.getText() == null) ? "" : txtPacienteFiltro.getText().trim();

            Usuario prof = cbNotaProfissional.getValue();
            Integer profissionalId = (prof == null) ? null : prof.getId();

            String forma = cbNotaForma.getValue();
            if (FORMA_TODAS.equals(forma)) forma = null;

            List<NotaDAO.NotaResumo> lista = notaDAO.listarNotasResumo(ini, fim, pacienteLike, profissionalId, forma);
            notasObs.setAll(lista);

        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao buscar notas", e.getMessage());
        }
    }

    @FXML
    private void onExportarNotasPdf() {
        if (notasObs.isEmpty()) { aviso("Nada para exportar", "Busque um relatório antes de exportar."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar relatório de notas (PDF)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("relatorio_notas.pdf");

        File destino = fc.showSaveDialog(tblNotas.getScene().getWindow());
        if (destino == null) return;

        try {
            String pacienteLike = (txtPacienteFiltro.getText() == null) ? "" : txtPacienteFiltro.getText().trim();

            Usuario prof = cbNotaProfissional.getValue();
            String profTxt = (prof == null) ? "" : nomeProf(prof);

            String forma = cbNotaForma.getValue();
            if (FORMA_TODAS.equals(forma)) forma = "";

            relNotasPdf.gerar(dtNotaInicio.getValue(), dtNotaFim.getValue(),
                    pacienteLike, profTxt, forma, notasObs, destino);

            aviso("Concluído", "PDF gerado em:\n" + destino.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao gerar PDF", e.getMessage());
        }
    }

    @FXML
    private void onReimprimirNota() {
        NotaDAO.NotaResumo sel = tblNotas.getSelectionModel().getSelectedItem();
        if (sel == null) { aviso("Selecione uma nota", "Selecione uma nota na tabela para reimprimir."); return; }

        try {
            Nota nota = notaDAO.buscarNotaCompleta(sel.getId());

            FileChooser fc = new FileChooser();
            fc.setTitle("Salvar PDF da nota");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            fc.setInitialFileName("nota_ID" + nota.getId() + ".pdf");

            File destino = fc.showSaveDialog(tblNotas.getScene().getWindow());
            if (destino == null) return;

            notaPdfService.gerarRecibo(nota, destino);
            aviso("Reimpressão concluída", "PDF gerado em:\n" + destino.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao reimprimir", e.getMessage());
        }
    }

    // helpers

    private String nvl(String s) { return s == null ? "" : s; }

    private void erro(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erro");
        a.setHeaderText(titulo);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void aviso(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}