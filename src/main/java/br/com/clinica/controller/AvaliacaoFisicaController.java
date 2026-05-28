package br.com.clinica.controller;

import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.AvaliacaoFisicaCalculoService;
import br.com.clinica.service.AvaliacaoFisicaService;
import br.com.clinica.service.DialogService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AvaliacaoFisicaController {

    @FXML private ComboBox<Paciente> cbPacientes;
    @FXML private ComboBox<Usuario> cbProfissionais;
    @FXML private DatePicker dpDataAvaliacao;
    @FXML private TextField txtObjetivo;

    @FXML private TextField txtPeso;
    @FXML private TextField txtAltura;
    @FXML private ComboBox<String> cbSexo;
    @FXML private TextField txtIdade;
    @FXML private ComboBox<String> cbNivelAtividade;

    @FXML private TextField txtDobraTricipital;
    @FXML private TextField txtDobraBicipital;
    @FXML private TextField txtDobraAbdominal;
    @FXML private TextField txtDobraSubescapular;
    @FXML private TextField txtDobraAxilarMedia;
    @FXML private TextField txtDobraCoxa;
    @FXML private TextField txtDobraToracica;
    @FXML private TextField txtDobraSuprailiaca;
    @FXML private TextField txtDobraPanturrilha;

    @FXML private TextField txtCircPescoco;
    @FXML private TextField txtCircTorax;
    @FXML private TextField txtCircOmbro;
    @FXML private TextField txtCircCintura;
    @FXML private TextField txtCircQuadril;
    @FXML private TextField txtCircAbdomen;
    @FXML private TextField txtCircBracoEsqRelaxado;
    @FXML private TextField txtCircBracoDirRelaxado;
    @FXML private TextField txtCircBracoEsqContraido;
    @FXML private TextField txtCircBracoDirContraido;
    @FXML private TextField txtCircAntebracoEsq;
    @FXML private TextField txtCircAntebracoDir;
    @FXML private TextField txtCircCoxaEsqProximal;
    @FXML private TextField txtCircCoxaDirProximal;
    @FXML private TextField txtCircCoxaEsqMedial;
    @FXML private TextField txtCircCoxaDirMedial;
    @FXML private TextField txtCircCoxaEsqDistal;
    @FXML private TextField txtCircCoxaDirDistal;
    @FXML private TextField txtCircPanturrilhaEsq;
    @FXML private TextField txtCircPanturrilhaDir;

    @FXML private TextArea txtObservacoesGerais;
    @FXML private TextArea txtObservacoesNutricionais;
    @FXML private TextArea txtAnotacoesProfissional;

    @FXML private Label lblImc;
    @FXML private Label lblClassificacaoImc;
    @FXML private Label lblPercentualGordura;
    @FXML private Label lblProtocolo;
    @FXML private Label lblMassaMagra;
    @FXML private Label lblMassaGorda;
    @FXML private Label lblRcq;
    @FXML private Label lblClassificacaoRcq;

    @FXML private TableView<AvaliacaoFisica> tableAvaliacoes;
    @FXML private TableColumn<AvaliacaoFisica, String> colData;
    @FXML private TableColumn<AvaliacaoFisica, String> colProfissional;
    @FXML private TableColumn<AvaliacaoFisica, Double> colPeso;
    @FXML private TableColumn<AvaliacaoFisica, Double> colImc;
    @FXML private TableColumn<AvaliacaoFisica, Double> colPercentual;
    @FXML private TableColumn<AvaliacaoFisica, Double> colMassaMagra;
    @FXML private TableColumn<AvaliacaoFisica, Double> colMassaGorda;
    @FXML private TableColumn<AvaliacaoFisica, Double> colRcq;

    @FXML private LineChart<String, Number> chartPeso;

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AvaliacaoFisicaService avaliacaoService = new AvaliacaoFisicaService();
    private final AvaliacaoFisicaCalculoService calculoService = new AvaliacaoFisicaCalculoService();
    private final DialogService dialogService = new DialogService();

    private AvaliacaoFisica avaliacaoSelecionada;

    private static final DateTimeFormatter DATA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        configurarCombos();
        configurarTabela();
        configurarMascarasNumericas();
        configurarEventos();
        carregarPacientes();
        carregarProfissionais();
        novaAvaliacao();
    }

    @FXML
    private void onNovaAvaliacao() {
        novaAvaliacao();
    }

    @FXML
    private void onSalvar() {
        try {
            AvaliacaoFisica avaliacao = montarAvaliacaoDoFormulario();

            avaliacaoService.salvar(avaliacao);

            dialogService.sucesso(
                    "Avaliação física salva",
                    "A avaliação física foi registrada com sucesso."
            );

            avaliacaoSelecionada = avaliacao;
            carregarHistoricoPaciente();

        } catch (IllegalArgumentException e) {
            dialogService.aviso("Dados inválidos", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao salvar avaliação física", e.getMessage());
        }
    }

    @FXML
    private void onExcluir() {
        AvaliacaoFisica selecionada = tableAvaliacoes.getSelectionModel().getSelectedItem();

        if (selecionada == null) {
            dialogService.aviso("Nenhuma avaliação selecionada", "Selecione uma avaliação no histórico para excluir.");
            return;
        }

        boolean confirmou = dialogService.confirmar(
                "Excluir avaliação física",
                "Confirmar exclusão",
                "Deseja realmente excluir esta avaliação física?"
        );

        if (!confirmou) return;

        try {
            avaliacaoService.excluir(selecionada.getId());
            dialogService.sucesso("Avaliação excluída", "A avaliação física foi removida com sucesso.");
            novaAvaliacao();
            carregarHistoricoPaciente();
        } catch (Exception e) {
            e.printStackTrace();
            dialogService.erro("Erro ao excluir", e.getMessage());
        }
    }

    private void configurarCombos() {
        cbSexo.setItems(FXCollections.observableArrayList("Masculino", "Feminino"));

        cbNivelAtividade.setItems(FXCollections.observableArrayList(
                "Sedentário",
                "Levemente ativo",
                "Moderadamente ativo",
                "Muito ativo",
                "Atleta"
        ));
    }

    private void configurarTabela() {
        colData.setCellValueFactory(cell -> {
            LocalDate data = cell.getValue().getDataAvaliacao();
            return new SimpleStringProperty(data == null ? "" : data.format(DATA_FORMATTER));
        });

        colProfissional.setCellValueFactory(cell ->
                new SimpleStringProperty(valorTexto(cell.getValue().getProfissionalResponsavel()))
        );

        colPeso.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPesoKg()));
        colImc.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getImc()));
        colPercentual.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPercentualGordura()));
        colMassaMagra.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getMassaMagraKg()));
        colMassaGorda.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getMassaGordaKg()));
        colRcq.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getRcq()));

        tableAvaliacoes.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, antigo, novo) -> {
                    if (novo != null) {
                        preencherFormulario(novo);
                    }
                });
    }

    private void configurarMascarasNumericas() {
        configurarCampoDecimalAutomatico(txtPeso);
        configurarCampoDecimalAutomatico(txtAltura);

        configurarCampoDecimalAutomatico(txtDobraTricipital);
        configurarCampoDecimalAutomatico(txtDobraBicipital);
        configurarCampoDecimalAutomatico(txtDobraAbdominal);
        configurarCampoDecimalAutomatico(txtDobraSubescapular);
        configurarCampoDecimalAutomatico(txtDobraAxilarMedia);
        configurarCampoDecimalAutomatico(txtDobraCoxa);
        configurarCampoDecimalAutomatico(txtDobraToracica);
        configurarCampoDecimalAutomatico(txtDobraSuprailiaca);
        configurarCampoDecimalAutomatico(txtDobraPanturrilha);

        configurarCampoDecimalAutomatico(txtCircPescoco);
        configurarCampoDecimalAutomatico(txtCircTorax);
        configurarCampoDecimalAutomatico(txtCircOmbro);
        configurarCampoDecimalAutomatico(txtCircCintura);
        configurarCampoDecimalAutomatico(txtCircQuadril);
        configurarCampoDecimalAutomatico(txtCircAbdomen);

        configurarCampoDecimalAutomatico(txtCircBracoEsqRelaxado);
        configurarCampoDecimalAutomatico(txtCircBracoDirRelaxado);
        configurarCampoDecimalAutomatico(txtCircBracoEsqContraido);
        configurarCampoDecimalAutomatico(txtCircBracoDirContraido);

        configurarCampoDecimalAutomatico(txtCircAntebracoEsq);
        configurarCampoDecimalAutomatico(txtCircAntebracoDir);

        configurarCampoDecimalAutomatico(txtCircCoxaEsqProximal);
        configurarCampoDecimalAutomatico(txtCircCoxaDirProximal);
        configurarCampoDecimalAutomatico(txtCircCoxaEsqMedial);
        configurarCampoDecimalAutomatico(txtCircCoxaDirMedial);
        configurarCampoDecimalAutomatico(txtCircCoxaEsqDistal);
        configurarCampoDecimalAutomatico(txtCircCoxaDirDistal);

        configurarCampoDecimalAutomatico(txtCircPanturrilhaEsq);
        configurarCampoDecimalAutomatico(txtCircPanturrilhaDir);

        configurarCampoInteiro(txtIdade);
    }

    private void configurarCampoDecimalAutomatico(TextField campo) {
        if (campo == null) {
            return;
        }

        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isBlank()) {
                return;
            }

            String numeros = newValue.replaceAll("[^\\d]", "");

            if (numeros.isEmpty()) {
                campo.setText("");
                return;
            }

            try {
                double valor = Double.parseDouble(numeros) / 100.0;

                String formatado = String.format("%.2f", valor).replace(".", ",");

                if (!formatado.equals(newValue)) {
                    campo.setText(formatado);
                    campo.positionCaret(formatado.length());
                }

            } catch (Exception ignored) {
            }
        });
    }

    private void configurarCampoInteiro(TextField campo) {
        if (campo == null) {
            return;
        }

        campo.setTextFormatter(new TextFormatter<>(change -> {
            String texto = change.getControlNewText();

            if (texto.matches("\\d{0,3}")) {
                return change;
            }

            return null;
        }));
    }

    private void configurarEventos() {
        cbPacientes.valueProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                preencherIdadePorPaciente(novo);
                carregarHistoricoPaciente();
            }
        });

        txtPeso.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtAltura.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtIdade.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        cbSexo.valueProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());

        txtDobraTricipital.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraBicipital.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraAbdominal.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraSubescapular.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraAxilarMedia.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraCoxa.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraToracica.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraSuprailiaca.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtDobraPanturrilha.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());

        txtCircCintura.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
        txtCircQuadril.textProperty().addListener((obs, o, n) -> atualizarPreviewCalculos());
    }

    private void carregarPacientes() {
        List<Paciente> pacientes = pacienteDAO.listarAtivos();
        cbPacientes.setItems(FXCollections.observableArrayList(pacientes));
    }

    private void carregarProfissionais() {
        List<Usuario> profissionais = usuarioDAO.listarProfissionaisAtivos();
        cbProfissionais.setItems(FXCollections.observableArrayList(profissionais));
    }

    private void novaAvaliacao() {
        avaliacaoSelecionada = null;

        dpDataAvaliacao.setValue(LocalDate.now());
        cbProfissionais.setValue(null);
        txtObjetivo.clear();

        txtPeso.clear();
        txtAltura.clear();
        cbSexo.setValue(null);
        txtIdade.clear();
        cbNivelAtividade.setValue(null);

        limparDobras();
        limparCircunferencias();

        txtObservacoesGerais.clear();
        txtObservacoesNutricionais.clear();
        txtAnotacoesProfissional.clear();

        limparResultados();
        tableAvaliacoes.getSelectionModel().clearSelection();

        Paciente paciente = cbPacientes.getValue();
        if (paciente != null) {
            preencherIdadePorPaciente(paciente);
        }
    }

    private void limparDobras() {
        txtDobraTricipital.clear();
        txtDobraBicipital.clear();
        txtDobraAbdominal.clear();
        txtDobraSubescapular.clear();
        txtDobraAxilarMedia.clear();
        txtDobraCoxa.clear();
        txtDobraToracica.clear();
        txtDobraSuprailiaca.clear();
        txtDobraPanturrilha.clear();
    }

    private void limparCircunferencias() {
        txtCircPescoco.clear();
        txtCircTorax.clear();
        txtCircOmbro.clear();
        txtCircCintura.clear();
        txtCircQuadril.clear();
        txtCircAbdomen.clear();

        txtCircBracoEsqRelaxado.clear();
        txtCircBracoDirRelaxado.clear();
        txtCircBracoEsqContraido.clear();
        txtCircBracoDirContraido.clear();

        txtCircAntebracoEsq.clear();
        txtCircAntebracoDir.clear();

        txtCircCoxaEsqProximal.clear();
        txtCircCoxaDirProximal.clear();
        txtCircCoxaEsqMedial.clear();
        txtCircCoxaDirMedial.clear();
        txtCircCoxaEsqDistal.clear();
        txtCircCoxaDirDistal.clear();

        txtCircPanturrilhaEsq.clear();
        txtCircPanturrilhaDir.clear();
    }

    private void limparResultados() {
        lblImc.setText("-");
        lblClassificacaoImc.setText("-");
        lblPercentualGordura.setText("-");
        lblProtocolo.setText("-");
        lblMassaMagra.setText("-");
        lblMassaGorda.setText("-");
        lblRcq.setText("-");
        lblClassificacaoRcq.setText("-");
    }

    private AvaliacaoFisica montarAvaliacaoDoFormulario() {
        AvaliacaoFisica a = avaliacaoSelecionada == null ? new AvaliacaoFisica() : avaliacaoSelecionada;

        Paciente paciente = cbPacientes.getValue();
        if (paciente != null) {
            a.setPaciente(paciente);
            a.setPacienteId(paciente.getId());
        }

        Usuario profissional = cbProfissionais.getValue();

        a.setDataAvaliacao(dpDataAvaliacao.getValue());
        a.setProfissionalResponsavel(profissional == null ? null : profissional.toString());
        a.setObjetivoPaciente(txtObjetivo.getText());

        a.setPesoKg(parseDouble(txtPeso));
        a.setAlturaM(parseDouble(txtAltura));
        a.setSexoBiologico(cbSexo.getValue());
        a.setIdadeNoMomento(parseInteger(txtIdade));
        a.setNivelAtividadeFisica(cbNivelAtividade.getValue());

        a.setDobraTricipitalMm(parseDouble(txtDobraTricipital));
        a.setDobraBicipitalMm(parseDouble(txtDobraBicipital));
        a.setDobraAbdominalMm(parseDouble(txtDobraAbdominal));
        a.setDobraSubescapularMm(parseDouble(txtDobraSubescapular));
        a.setDobraAxilarMediaMm(parseDouble(txtDobraAxilarMedia));
        a.setDobraCoxaMm(parseDouble(txtDobraCoxa));
        a.setDobraToracicaMm(parseDouble(txtDobraToracica));
        a.setDobraSuprailiacaMm(parseDouble(txtDobraSuprailiaca));
        a.setDobraPanturrilhaMm(parseDouble(txtDobraPanturrilha));

        a.setCircPescocoCm(parseDouble(txtCircPescoco));
        a.setCircToraxCm(parseDouble(txtCircTorax));
        a.setCircOmbroCm(parseDouble(txtCircOmbro));
        a.setCircCinturaCm(parseDouble(txtCircCintura));
        a.setCircQuadrilCm(parseDouble(txtCircQuadril));
        a.setCircAbdomenCm(parseDouble(txtCircAbdomen));

        a.setCircBracoEsqRelaxadoCm(parseDouble(txtCircBracoEsqRelaxado));
        a.setCircBracoDirRelaxadoCm(parseDouble(txtCircBracoDirRelaxado));
        a.setCircBracoEsqContraidoCm(parseDouble(txtCircBracoEsqContraido));
        a.setCircBracoDirContraidoCm(parseDouble(txtCircBracoDirContraido));

        a.setCircAntebracoEsqCm(parseDouble(txtCircAntebracoEsq));
        a.setCircAntebracoDirCm(parseDouble(txtCircAntebracoDir));

        a.setCircCoxaEsqProximalCm(parseDouble(txtCircCoxaEsqProximal));
        a.setCircCoxaDirProximalCm(parseDouble(txtCircCoxaDirProximal));
        a.setCircCoxaEsqMedialCm(parseDouble(txtCircCoxaEsqMedial));
        a.setCircCoxaDirMedialCm(parseDouble(txtCircCoxaDirMedial));
        a.setCircCoxaEsqDistalCm(parseDouble(txtCircCoxaEsqDistal));
        a.setCircCoxaDirDistalCm(parseDouble(txtCircCoxaDirDistal));

        a.setCircPanturrilhaEsqCm(parseDouble(txtCircPanturrilhaEsq));
        a.setCircPanturrilhaDirCm(parseDouble(txtCircPanturrilhaDir));

        a.setObservacoesGerais(txtObservacoesGerais.getText());
        a.setObservacoesNutricionais(txtObservacoesNutricionais.getText());
        a.setAnotacoesProfissional(txtAnotacoesProfissional.getText());

        return a;
    }

    private void preencherFormulario(AvaliacaoFisica a) {
        avaliacaoSelecionada = a;

        selecionarPacientePorId(a.getPacienteId());

        dpDataAvaliacao.setValue(a.getDataAvaliacao());
        txtObjetivo.setText(valorTextoParaCampo(a.getObjetivoPaciente()));

        txtPeso.setText(valorNumeroParaCampo(a.getPesoKg()));
        txtAltura.setText(valorNumeroParaCampo(a.getAlturaM()));
        cbSexo.setValue(a.getSexoBiologico());
        txtIdade.setText(a.getIdadeNoMomento() == null ? "" : String.valueOf(a.getIdadeNoMomento()));
        cbNivelAtividade.setValue(a.getNivelAtividadeFisica());

        txtDobraTricipital.setText(valorNumeroParaCampo(a.getDobraTricipitalMm()));
        txtDobraBicipital.setText(valorNumeroParaCampo(a.getDobraBicipitalMm()));
        txtDobraAbdominal.setText(valorNumeroParaCampo(a.getDobraAbdominalMm()));
        txtDobraSubescapular.setText(valorNumeroParaCampo(a.getDobraSubescapularMm()));
        txtDobraAxilarMedia.setText(valorNumeroParaCampo(a.getDobraAxilarMediaMm()));
        txtDobraCoxa.setText(valorNumeroParaCampo(a.getDobraCoxaMm()));
        txtDobraToracica.setText(valorNumeroParaCampo(a.getDobraToracicaMm()));
        txtDobraSuprailiaca.setText(valorNumeroParaCampo(a.getDobraSuprailiacaMm()));
        txtDobraPanturrilha.setText(valorNumeroParaCampo(a.getDobraPanturrilhaMm()));

        txtCircPescoco.setText(valorNumeroParaCampo(a.getCircPescocoCm()));
        txtCircTorax.setText(valorNumeroParaCampo(a.getCircToraxCm()));
        txtCircOmbro.setText(valorNumeroParaCampo(a.getCircOmbroCm()));
        txtCircCintura.setText(valorNumeroParaCampo(a.getCircCinturaCm()));
        txtCircQuadril.setText(valorNumeroParaCampo(a.getCircQuadrilCm()));
        txtCircAbdomen.setText(valorNumeroParaCampo(a.getCircAbdomenCm()));

        txtCircBracoEsqRelaxado.setText(valorNumeroParaCampo(a.getCircBracoEsqRelaxadoCm()));
        txtCircBracoDirRelaxado.setText(valorNumeroParaCampo(a.getCircBracoDirRelaxadoCm()));
        txtCircBracoEsqContraido.setText(valorNumeroParaCampo(a.getCircBracoEsqContraidoCm()));
        txtCircBracoDirContraido.setText(valorNumeroParaCampo(a.getCircBracoDirContraidoCm()));

        txtCircAntebracoEsq.setText(valorNumeroParaCampo(a.getCircAntebracoEsqCm()));
        txtCircAntebracoDir.setText(valorNumeroParaCampo(a.getCircAntebracoDirCm()));

        txtCircCoxaEsqProximal.setText(valorNumeroParaCampo(a.getCircCoxaEsqProximalCm()));
        txtCircCoxaDirProximal.setText(valorNumeroParaCampo(a.getCircCoxaDirProximalCm()));
        txtCircCoxaEsqMedial.setText(valorNumeroParaCampo(a.getCircCoxaEsqMedialCm()));
        txtCircCoxaDirMedial.setText(valorNumeroParaCampo(a.getCircCoxaDirMedialCm()));
        txtCircCoxaEsqDistal.setText(valorNumeroParaCampo(a.getCircCoxaEsqDistalCm()));
        txtCircCoxaDirDistal.setText(valorNumeroParaCampo(a.getCircCoxaDirDistalCm()));

        txtCircPanturrilhaEsq.setText(valorNumeroParaCampo(a.getCircPanturrilhaEsqCm()));
        txtCircPanturrilhaDir.setText(valorNumeroParaCampo(a.getCircPanturrilhaDirCm()));

        txtObservacoesGerais.setText(valorTextoParaCampo(a.getObservacoesGerais()));
        txtObservacoesNutricionais.setText(valorTextoParaCampo(a.getObservacoesNutricionais()));
        txtAnotacoesProfissional.setText(valorTextoParaCampo(a.getAnotacoesProfissional()));

        atualizarLabelsResultado(a);
    }

    private void atualizarPreviewCalculos() {
        try {
            AvaliacaoFisica preview = montarAvaliacaoDoFormulario();
            calculoService.calcularResultados(preview);
            atualizarLabelsResultado(preview);
        } catch (Exception ignored) {
            limparResultados();
        }
    }

    private void atualizarLabelsResultado(AvaliacaoFisica a) {
        lblImc.setText(valorNumero(a.getImc()));
        lblClassificacaoImc.setText(valorTexto(a.getClassificacaoImc()));
        lblPercentualGordura.setText(valorNumero(a.getPercentualGordura()));
        lblProtocolo.setText(valorTexto(a.getProtocoloGordura()));
        lblMassaMagra.setText(valorNumero(a.getMassaMagraKg()));
        lblMassaGorda.setText(valorNumero(a.getMassaGordaKg()));
        lblRcq.setText(valorNumero(a.getRcq()));
        lblClassificacaoRcq.setText(valorTexto(a.getClassificacaoRcq()));
    }

    private void carregarHistoricoPaciente() {
        Paciente paciente = cbPacientes.getValue();

        if (paciente == null || paciente.getId() == null) {
            tableAvaliacoes.setItems(FXCollections.observableArrayList());
            chartPeso.getData().clear();
            return;
        }

        List<AvaliacaoFisica> historico = avaliacaoService.listarPorPaciente(paciente.getId());

        tableAvaliacoes.setItems(FXCollections.observableArrayList(historico));
        atualizarGraficoPeso(historico);
    }

    private void atualizarGraficoPeso(List<AvaliacaoFisica> historico) {
        chartPeso.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Peso");

        for (int i = historico.size() - 1; i >= 0; i--) {
            AvaliacaoFisica a = historico.get(i);

            if (a.getDataAvaliacao() != null && a.getPesoKg() != null) {
                serie.getData().add(new XYChart.Data<>(
                        a.getDataAvaliacao().format(DATA_FORMATTER),
                        a.getPesoKg()
                ));
            }
        }

        chartPeso.getData().add(serie);
    }

    private void preencherIdadePorPaciente(Paciente paciente) {
        if (paciente != null && paciente.getDataNascimento() != null) {
            txtIdade.setText(String.valueOf(paciente.getIdade()));
        }
    }

    private void selecionarPacientePorId(Long pacienteId) {
        if (pacienteId == null) return;

        for (Paciente p : cbPacientes.getItems()) {
            if (pacienteId.equals(p.getId())) {
                cbPacientes.setValue(p);
                return;
            }
        }
    }

    private Double parseDouble(TextInputControl campo) {
        String texto = campo.getText();

        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            texto = texto.trim().replace(".", "").replace(",", ".");
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe um número válido no campo: " + campo.getPromptText());
        }
    }

    private Integer parseInteger(TextInputControl campo) {
        String texto = campo.getText();

        if (texto == null || texto.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Informe uma idade válida.");
        }
    }

    private String valorNumero(Double valor) {
        return valor == null ? "-" : String.format("%.2f", valor);
    }

    private String valorNumeroParaCampo(Double valor) {
        return valor == null ? "" : String.format("%.2f", valor).replace(".", ",");
    }

    private String valorTexto(String valor) {
        return valor == null || valor.isBlank() ? "-" : valor;
    }

    private String valorTextoParaCampo(String valor) {
        return valor == null || valor.isBlank() || "-".equals(valor) ? "" : valor;
    }
}