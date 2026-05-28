package br.com.clinica.controller;

import br.com.clinica.dao.AnexoPacienteDAO.AnexoPacienteItem;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.model.Paciente;
import br.com.clinica.service.AvaliacaoFisicaService;
import br.com.clinica.service.DialogService;
import br.com.clinica.service.PacienteHistoricoAnamneseService;
import br.com.clinica.service.PacienteHistoricoAnexoService;
import br.com.clinica.service.PacienteHistoricoHeaderService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PacienteHistoricoController {

    @FXML private Label lblNome;
    @FXML private Label lblCpf;
    @FXML private Label lblRg;
    @FXML private Label lblTelefone;
    @FXML private Label lblDataNascimento;
    @FXML private Label lblIdade;
    @FXML private Label lblEndereco;
    @FXML private Label lblAtivo;

    @FXML private TableView<AnexoPacienteItem> tableAnexos;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoNome;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoDescricao;
    @FXML private TableColumn<AnexoPacienteItem, String> colAnexoDataHora;
    @FXML private Button btnAbrirAnexo;
    @FXML private Button btnAtualizarAnexos;

    @FXML private TableView<Anamnese> tableAnamnese;
    @FXML private TableColumn<Anamnese, String> colAnaDataHora;
    @FXML private TableColumn<Anamnese, String> colAnaTipo;

    @FXML private TextArea txtObservacoes;
    @FXML private TextArea txtResumo;
    @FXML private TextArea txtJson;
    @FXML private TitledPane tpJson;

    @FXML private Button btnAtualizarAnamnese;

    @FXML private TableView<AvaliacaoFisica> tableAvaliacoesFisicas;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfData;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfProfissional;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfPeso;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfImc;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfGordura;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfMassaMagra;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfMassaGorda;
    @FXML private TableColumn<AvaliacaoFisica, String> colAfRcq;

    @FXML private Label lblAfUltimaData;
    @FXML private Label lblAfUltimoPeso;
    @FXML private Label lblAfUltimoImc;
    @FXML private Label lblAfUltimaGordura;
    @FXML private Label lblAfUltimaMassaMagra;
    @FXML private Label lblAfUltimaMassaGorda;
    @FXML private Label lblAfUltimoRcq;
    @FXML private TextArea txtResumoAvaliacaoFisica;

    @FXML private Button btnAtualizarAvaliacoesFisicas;

    private final PacienteHistoricoHeaderService headerService = new PacienteHistoricoHeaderService();
    private final PacienteHistoricoAnexoService anexoService = new PacienteHistoricoAnexoService();
    private final PacienteHistoricoAnamneseService anamneseService = new PacienteHistoricoAnamneseService();
    private final AvaliacaoFisicaService avaliacaoFisicaService = new AvaliacaoFisicaService();
    private final DialogService dialogService = new DialogService();

    private final ObservableList<AnexoPacienteItem> anexos = FXCollections.observableArrayList();
    private final ObservableList<Anamnese> anamneses = FXCollections.observableArrayList();
    private final ObservableList<AvaliacaoFisica> avaliacoesFisicas = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Paciente paciente;

    @FXML
    private void initialize() {
        configurarTabelaAnexos();
        configurarTabelaAnamnese();
        configurarTabelaAvaliacoesFisicas();
        configurarCamposDetalhe();
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;

        preencherCabecalho();
        carregarAnexos();
        carregarAnamneses();
        carregarAvaliacoesFisicas();
    }

    private void configurarTabelaAnexos() {
        if (colAnexoNome != null) {
            colAnexoNome.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getNomeArquivo()))
            );
        }

        if (colAnexoDescricao != null) {
            colAnexoDescricao.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDescricao()))
            );
        }

        if (colAnexoDataHora != null) {
            colAnexoDataHora.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDataHora()))
            );
        }

        if (tableAnexos != null) {
            tableAnexos.setItems(anexos);
            tableAnexos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                    atualizarEstadoBotaoAbrirAnexo()
            );
        }

        atualizarEstadoBotaoAbrirAnexo();
    }

    private void configurarTabelaAnamnese() {
        if (colAnaDataHora != null) {
            colAnaDataHora.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDataHora()))
            );
        }

        if (colAnaTipo != null) {
            colAnaTipo.setCellValueFactory(c ->
                    new SimpleStringProperty(anamneseService.formatTipo(safe(c.getValue().getTipo())))
            );
        }

        if (tableAnamnese != null) {
            tableAnamnese.setItems(anamneses);
            tableAnamnese.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                    preencherDetalheAnamnese(newSel)
            );
        }
    }

    private void configurarTabelaAvaliacoesFisicas() {
        if (colAfData != null) {
            colAfData.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarData(c.getValue().getDataAvaliacao()))
            );
        }

        if (colAfProfissional != null) {
            colAfProfissional.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getProfissionalResponsavel()))
            );
        }

        if (colAfPeso != null) {
            colAfPeso.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarNumero(c.getValue().getPesoKg()))
            );
        }

        if (colAfImc != null) {
            colAfImc.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarNumero(c.getValue().getImc()))
            );
        }

        if (colAfGordura != null) {
            colAfGordura.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarNumero(c.getValue().getPercentualGordura()))
            );
        }

        if (colAfMassaMagra != null) {
            colAfMassaMagra.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarNumero(c.getValue().getMassaMagraKg()))
            );
        }

        if (colAfMassaGorda != null) {
            colAfMassaGorda.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarNumero(c.getValue().getMassaGordaKg()))
            );
        }

        if (colAfRcq != null) {
            colAfRcq.setCellValueFactory(c ->
                    new SimpleStringProperty(formatarNumero(c.getValue().getRcq()))
            );
        }

        if (tableAvaliacoesFisicas != null) {
            tableAvaliacoesFisicas.setItems(avaliacoesFisicas);
            tableAvaliacoesFisicas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) ->
                    preencherResumoAvaliacaoFisica(newSel)
            );
        }
    }

    private void configurarCamposDetalhe() {
        setEditable(txtObservacoes, false);
        setEditable(txtResumo, false);
        setEditable(txtJson, false);
        setEditable(txtResumoAvaliacaoFisica, false);

        ocultarJsonTecnico();
    }

    private void preencherCabecalho() {
        headerService.preencherCabecalho(
                paciente,
                lblNome,
                lblCpf,
                lblRg,
                lblTelefone,
                lblDataNascimento,
                lblIdade,
                lblEndereco,
                lblAtivo
        );
    }

    @FXML
    private void onAtualizarAnexos() {
        carregarAnexos();
    }

    private void carregarAnexos() {
        anexos.clear();

        if (!pacienteValido()) {
            atualizarEstadoBotaoAbrirAnexo();
            return;
        }

        try {
            List<AnexoPacienteItem> list = anexoService.listarPorPaciente(paciente.getId());
            anexos.addAll(list);

            if (tableAnexos != null) {
                tableAnexos.getSelectionModel().clearSelection();
            }

            atualizarEstadoBotaoAbrirAnexo();

        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao carregar anexos", "Não foi possível carregar os anexos do paciente.");
        }
    }

    @FXML
    private void onAbrirAnexo() {
        AnexoPacienteItem item = getAnexoSelecionado();

        if (item == null) {
            dialogService.erro("Atenção", "Selecione um anexo para abrir.");
            return;
        }

        try {
            anexoService.abrirAnexo(item);
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao abrir anexo", e.getMessage());
        }
    }

    private AnexoPacienteItem getAnexoSelecionado() {
        if (tableAnexos == null) {
            return null;
        }

        return tableAnexos.getSelectionModel().getSelectedItem();
    }

    private void atualizarEstadoBotaoAbrirAnexo() {
        if (btnAbrirAnexo != null) {
            btnAbrirAnexo.setDisable(getAnexoSelecionado() == null);
        }
    }

    @FXML
    private void onAtualizarAnamnese() {
        carregarAnamneses();
    }

    private void carregarAnamneses() {
        anamneses.clear();
        limparDetalheAnamnese();

        if (!pacienteValido()) {
            return;
        }

        try {
            List<Anamnese> list = anamneseService.listarPorPaciente(paciente.getId());
            anamneses.addAll(list);

            if (tableAnamnese != null) {
                tableAnamnese.getSelectionModel().clearSelection();
            }

        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao carregar histórico", "Não foi possível carregar o histórico clínico do paciente.");
        }
    }

    @FXML
    private void onAtualizarAvaliacoesFisicas() {
        carregarAvaliacoesFisicas();
    }

    private void carregarAvaliacoesFisicas() {
        avaliacoesFisicas.clear();
        limparResumoAvaliacaoFisica();
        limparCardsAvaliacaoFisica();

        if (!pacienteValido()) {
            return;
        }

        try {
            List<AvaliacaoFisica> list = avaliacaoFisicaService.listarPorPaciente(paciente.getId());
            avaliacoesFisicas.addAll(list);

            if (!list.isEmpty()) {
                AvaliacaoFisica ultima = list.get(0);
                preencherCardsUltimaAvaliacao(ultima);
                preencherResumoAvaliacaoFisica(ultima);
            }

            if (tableAvaliacoesFisicas != null) {
                tableAvaliacoesFisicas.getSelectionModel().clearSelection();
            }

        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao carregar avaliações físicas", "Não foi possível carregar as avaliações físicas do paciente.");
        }
    }

    private void preencherCardsUltimaAvaliacao(AvaliacaoFisica a) {
        setText(lblAfUltimaData, formatarData(a.getDataAvaliacao()));
        setText(lblAfUltimoPeso, formatarNumeroComUnidade(a.getPesoKg(), " kg"));
        setText(lblAfUltimoImc, formatarNumero(a.getImc()));
        setText(lblAfUltimaGordura, formatarNumeroComUnidade(a.getPercentualGordura(), "%"));
        setText(lblAfUltimaMassaMagra, formatarNumeroComUnidade(a.getMassaMagraKg(), " kg"));
        setText(lblAfUltimaMassaGorda, formatarNumeroComUnidade(a.getMassaGordaKg(), " kg"));
        setText(lblAfUltimoRcq, formatarNumero(a.getRcq()));
    }

    private void preencherResumoAvaliacaoFisica(AvaliacaoFisica a) {
        if (a == null) {
            limparResumoAvaliacaoFisica();
            return;
        }

        String resumo = """
                Data da avaliação: %s
                Profissional responsável: %s

                Peso: %s kg
                Altura: %s m
                IMC: %s
                Classificação IMC: %s

                Percentual de gordura: %s%%
                Protocolo utilizado: %s
                Massa magra: %s kg
                Massa gorda: %s kg

                RCQ: %s
                Classificação RCQ: %s

                Objetivo do paciente:
                %s

                Nível de atividade física:
                %s

                Observações gerais:
                %s

                Observações nutricionais:
                %s

                Anotações do profissional:
                %s
                """.formatted(
                formatarData(a.getDataAvaliacao()),
                safe(a.getProfissionalResponsavel()),
                formatarNumero(a.getPesoKg()),
                formatarNumero(a.getAlturaM()),
                formatarNumero(a.getImc()),
                safe(a.getClassificacaoImc()),
                formatarNumero(a.getPercentualGordura()),
                safe(a.getProtocoloGordura()),
                formatarNumero(a.getMassaMagraKg()),
                formatarNumero(a.getMassaGordaKg()),
                formatarNumero(a.getRcq()),
                safe(a.getClassificacaoRcq()),
                safe(a.getObjetivoPaciente()),
                safe(a.getNivelAtividadeFisica()),
                safe(a.getObservacoesGerais()),
                safe(a.getObservacoesNutricionais()),
                safe(a.getAnotacoesProfissional())
        );

        setText(txtResumoAvaliacaoFisica, resumo);
    }

    private void limparCardsAvaliacaoFisica() {
        setText(lblAfUltimaData, "-");
        setText(lblAfUltimoPeso, "-");
        setText(lblAfUltimoImc, "-");
        setText(lblAfUltimaGordura, "-");
        setText(lblAfUltimaMassaMagra, "-");
        setText(lblAfUltimaMassaGorda, "-");
        setText(lblAfUltimoRcq, "-");
    }

    private void limparResumoAvaliacaoFisica() {
        clear(txtResumoAvaliacaoFisica);
    }

    private void preencherDetalheAnamnese(Anamnese anamnese) {
        if (anamnese == null) {
            limparDetalheAnamnese();
            return;
        }

        setText(txtObservacoes, safe(anamnese.getObservacoes()));
        setText(txtResumo, anamneseService.montarResumo(anamnese));
        setText(txtJson, "");

        ocultarJsonTecnico();
    }

    private void limparDetalheAnamnese() {
        clear(txtObservacoes);
        clear(txtResumo);
        clear(txtJson);

        ocultarJsonTecnico();
    }

    private void ocultarJsonTecnico() {
        if (tpJson != null) {
            tpJson.setExpanded(false);
            tpJson.setVisible(false);
            tpJson.setManaged(false);
        }
    }

    @FXML
    private void onFechar() {
        if (lblNome != null && lblNome.getScene() != null) {
            lblNome.getScene().getWindow().hide();
        }
    }

    private boolean pacienteValido() {
        return paciente != null && paciente.getId() != null;
    }

    private void setEditable(TextArea textArea, boolean editable) {
        if (textArea != null) {
            textArea.setEditable(editable);
        }
    }

    private void setText(TextArea textArea, String texto) {
        if (textArea != null) {
            textArea.setText(texto == null ? "" : texto);
        }
    }

    private void setText(Label label, String texto) {
        if (label != null) {
            label.setText(texto == null || texto.isBlank() ? "-" : texto);
        }
    }

    private void clear(TextArea textArea) {
        if (textArea != null) {
            textArea.clear();
        }
    }

    private String safe(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }

    private String formatarData(LocalDate data) {
        return data == null ? "-" : data.format(DATA_FORMATTER);
    }

    private String formatarNumero(Double valor) {
        return valor == null ? "-" : String.format("%.2f", valor);
    }

    private String formatarNumeroComUnidade(Double valor, String unidade) {
        return valor == null ? "-" : String.format("%.2f%s", valor, unidade);
    }
}