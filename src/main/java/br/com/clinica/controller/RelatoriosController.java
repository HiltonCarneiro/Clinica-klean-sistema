package br.com.clinica.controller;

import br.com.clinica.dao.NotaDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.Nota;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.NotaPdfService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class RelatoriosController {

    // ===================== ABA RELATÓRIO =====================
    @FXML private DatePicker dtRelInicio;
    @FXML private DatePicker dtRelFim;
    @FXML private ComboBox<String> cbAgrupamento;
    @FXML private ComboBox<Usuario> cbRelProfissional;

    @FXML private TableView<NotaDAO.RelatorioRow> tblRelatorio;
    @FXML private TableColumn<NotaDAO.RelatorioRow, String> colRelChave;
    @FXML private TableColumn<NotaDAO.RelatorioRow, Double> colRelTotal;

    @FXML private Label lblRelTotal;

    private final ObservableList<NotaDAO.RelatorioRow> relatorioObs = FXCollections.observableArrayList();

    // ===================== ABA NOTAS =====================
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

    // ===================== DAOs / Services =====================
    private final NotaDAO notaDAO = new NotaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final NotaPdfService notaPdfService = new NotaPdfService();

    private static final String FORMA_TODAS = "TODAS";

    @FXML
    public void initialize() {
        LocalDate hoje = LocalDate.now();

        // Datas padrão
        dtRelInicio.setValue(hoje);
        dtRelFim.setValue(hoje);
        dtNotaInicio.setValue(hoje);
        dtNotaFim.setValue(hoje);

        // Agrupamentos
        cbAgrupamento.setItems(FXCollections.observableArrayList("DIARIO", "MENSAL", "PROFISSIONAL"));
        cbAgrupamento.getSelectionModel().select("DIARIO");

        // Profissionais (com opção "Todos" via item null)
        List<Usuario> profissionais = usuarioDAO.listarProfissionaisAtivos();

        ObservableList<Usuario> profObs = FXCollections.observableArrayList();
        profObs.add(null); // "Todos"
        profObs.addAll(profissionais);

        cbRelProfissional.setItems(profObs);
        cbNotaProfissional.setItems(profObs);

        // Mostrar "Todos" quando for null
        configurarComboProfissionais(cbRelProfissional);
        configurarComboProfissionais(cbNotaProfissional);

        cbRelProfissional.getSelectionModel().selectFirst();
        cbNotaProfissional.getSelectionModel().selectFirst();

        // Formas de pagamento (sem null pra não virar ObservableList<Object>)
        ObservableList<String> formas = FXCollections.observableArrayList(
                FORMA_TODAS, "DINHEIRO", "PIX", "CARTAO", "TRANSFERENCIA", "OUTRO"
        );
        cbNotaForma.setItems(formas);
        cbNotaForma.getSelectionModel().select(FORMA_TODAS);

        // Tabela relatório
        tblRelatorio.setItems(relatorioObs);
        colRelChave.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getChave()));
        colRelTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotal()).asObject());

        // Tabela notas
        tblNotas.setItems(notasObs);
        colNotaId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getId()).asObject());
        colNotaDataHora.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataHoraFmt()));
        colNotaPaciente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPacienteNome()));
        colNotaProfissional.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProfissionalNome()));
        colNotaForma.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFormaPagamento()));
        colNotaTotal.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotalLiquido()).asObject());

        // Carregar inicial
        onBuscarRelatorio();
        onBuscarNotas();
    }

    private void configurarComboProfissionais(ComboBox<Usuario> cb) {
        cb.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos");
                } else {
                    String pessoa = (item.getPessoaNome() != null && !item.getPessoaNome().isBlank())
                            ? item.getPessoaNome()
                            : item.getNome();
                    setText(pessoa);
                }
            }
        });

        cb.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else if (item == null) {
                    setText("Todos");
                } else {
                    String pessoa = (item.getPessoaNome() != null && !item.getPessoaNome().isBlank())
                            ? item.getPessoaNome()
                            : item.getNome();
                    setText(pessoa);
                }
            }
        });
    }

    // ===================== RELATÓRIO =====================

    @FXML
    private void onBuscarRelatorio() {
        try {
            LocalDate ini = dtRelInicio.getValue();
            LocalDate fim = dtRelFim.getValue();

            if (ini == null || fim == null) {
                erro("Período inválido", "Informe data inicial e final.");
                return;
            }
            if (fim.isBefore(ini)) {
                erro("Período inválido", "A data final não pode ser menor que a inicial.");
                return;
            }

            String agrup = cbAgrupamento.getValue();
            Usuario prof = cbRelProfissional.getValue();
            Integer profissionalId = (prof == null) ? null : prof.getId();

            List<NotaDAO.RelatorioRow> rows = notaDAO.relatorioEntradas(ini, fim, agrup, profissionalId);
            relatorioObs.setAll(rows);

            double total = rows.stream().mapToDouble(NotaDAO.RelatorioRow::getTotal).sum();
            lblRelTotal.setText(String.format("R$ %.2f", total));

        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao buscar relatório", e.getMessage());
        }
    }

    @FXML
    private void onExportarRelatorioCsv() {
        if (relatorioObs.isEmpty()) {
            aviso("Nada para exportar", "Busque um relatório antes de exportar.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Salvar relatório CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("relatorio_caixa.csv");

        File arq = fc.showSaveDialog(tblRelatorio.getScene().getWindow());
        if (arq == null) return;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("chave,total\n");
            for (NotaDAO.RelatorioRow r : relatorioObs) {
                sb.append(escapeCsv(r.getChave())).append(",")
                        .append(String.format("%.2f", r.getTotal()).replace(",", "."))
                        .append("\n");
            }
            Files.writeString(arq.toPath(), sb.toString(), StandardCharsets.UTF_8);
            aviso("Exportado", "CSV gerado em:\n" + arq.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao exportar CSV", e.getMessage());
        }
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        String t = s.replace("\"", "\"\"");
        if (t.contains(",") || t.contains("\n") || t.contains("\"")) return "\"" + t + "\"";
        return t;
    }

    // ===================== NOTAS =====================

    @FXML
    private void onBuscarNotas() {
        try {
            LocalDate ini = dtNotaInicio.getValue();
            LocalDate fim = dtNotaFim.getValue();

            if (ini == null || fim == null) {
                erro("Período inválido", "Informe data inicial e final.");
                return;
            }
            if (fim.isBefore(ini)) {
                erro("Período inválido", "A data final não pode ser menor que a inicial.");
                return;
            }

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
    private void onReimprimirNota() {
        NotaDAO.NotaResumo sel = tblNotas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            aviso("Selecione uma nota", "Selecione uma nota na tabela para reimprimir.");
            return;
        }

        try {
            Nota nota = notaDAO.buscarNotaCompleta(sel.getId());

            FileChooser fc = new FileChooser();
            fc.setTitle("Salvar PDF da nota");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

            String nomePaciente = (nota.getPaciente() != null && nota.getPaciente().getNome() != null)
                    ? nota.getPaciente().getNome().replaceAll("[^a-zA-Z0-9_\\- ]", "")
                    : "nota";

            fc.setInitialFileName("nota_" + nomePaciente + "_ID" + nota.getId() + ".pdf");

            File destino = fc.showSaveDialog(tblNotas.getScene().getWindow());
            if (destino == null) return;

            notaPdfService.gerarRecibo(nota, destino);
            aviso("Reimpressão concluída", "PDF gerado em:\n" + destino.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            erro("Erro ao reimprimir", e.getMessage());
        }
    }

    // ===================== UI helpers =====================

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