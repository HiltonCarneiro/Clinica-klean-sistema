package br.com.clinica.controller;

import br.com.clinica.auth.Permissao;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaChartManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaComboManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaFormFields;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaFormManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaHistoryManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaMaskManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaResultManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaSelectionManager;
import br.com.clinica.controller.avaliacaofisica.AvaliacaoFisicaTableManager;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.dao.UsuarioDAO;
import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.service.AvaliacaoFisicaCalculoService;
import br.com.clinica.service.AvaliacaoFisicaService;
import br.com.clinica.service.DialogService;
import br.com.clinica.service.PermissionService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.List;

public class    AvaliacaoFisicaController {

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
    private final PermissionService permissionService = new PermissionService();

    private AvaliacaoFisicaComboManager comboManager;
    private AvaliacaoFisicaTableManager tableManager;
    private AvaliacaoFisicaMaskManager maskManager;
    private AvaliacaoFisicaFormManager formManager;
    private AvaliacaoFisicaResultManager resultManager;
    private AvaliacaoFisicaChartManager chartManager;
    private AvaliacaoFisicaHistoryManager historyManager;
    private AvaliacaoFisicaSelectionManager selectionManager;

    @FXML
    private void initialize() {
        permissionService.exigir(Permissao.AVALIACAO_FISICA_VER);

        inicializarManagers();
        comboManager.configurarCombos(cbSexo, cbNivelAtividade);
        tableManager.configurarTabela(
                tableAvaliacoes,
                colData,
                colProfissional,
                colPeso,
                colImc,
                colPercentual,
                colMassaMagra,
                colMassaGorda,
                colRcq,
                this::preencherFormularioSelecionado
        );
        configurarMascarasNumericas();
        configurarEventos();
        comboManager.carregarPacientes(cbPacientes);
        comboManager.carregarProfissionais(cbProfissionais);
        novaAvaliacao();
    }

    @FXML
    private void onNovaAvaliacao() {
        permissionService.exigir(Permissao.AVALIACAO_FISICA_VER);
        novaAvaliacao();
    }

    @FXML
    private void onSalvar() {
        permissionService.exigir(Permissao.AVALIACAO_FISICA_VER);

        try {
            AvaliacaoFisica avaliacao = formManager.montarAvaliacaoDoFormulario();
            avaliacaoService.salvar(avaliacao);

            dialogService.sucesso(
                    "Avaliação física salva",
                    "A avaliação física foi registrada com sucesso."
            );

            formManager.setSelecionada(avaliacao);
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
        permissionService.exigir(Permissao.AVALIACAO_FISICA_VER);

        if (tableAvaliacoes == null) {
            dialogService.aviso("Histórico indisponível", "A tabela de avaliações não foi carregada corretamente.");
            return;
        }

        AvaliacaoFisica selecionada = selectionManager.obterSelecionada(tableAvaliacoes);

        if (selecionada == null) {
            dialogService.aviso("Nenhuma avaliação selecionada", "Selecione uma avaliação no histórico para excluir.");
            return;
        }

        boolean confirmou = dialogService.confirmar(
                "Excluir avaliação física",
                "Confirmar exclusão",
                "Deseja realmente excluir esta avaliação física?"
        );

        if (!confirmou) {
            return;
        }

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

    private void inicializarManagers() {
        comboManager = new AvaliacaoFisicaComboManager(pacienteDAO, usuarioDAO);
        tableManager = new AvaliacaoFisicaTableManager();
        maskManager = new AvaliacaoFisicaMaskManager();
        resultManager = new AvaliacaoFisicaResultManager(calculoService);
        chartManager = new AvaliacaoFisicaChartManager();
        historyManager = new AvaliacaoFisicaHistoryManager(avaliacaoService, tableManager, chartManager);
        selectionManager = new AvaliacaoFisicaSelectionManager();
        formManager = new AvaliacaoFisicaFormManager(criarFormFields(), comboManager, resultManager);
    }

    private AvaliacaoFisicaFormFields criarFormFields() {
        return new AvaliacaoFisicaFormFields(
                cbPacientes,
                cbProfissionais,
                dpDataAvaliacao,
                txtObjetivo,
                txtPeso,
                txtAltura,
                cbSexo,
                txtIdade,
                cbNivelAtividade,
                txtDobraTricipital,
                txtDobraBicipital,
                txtDobraAbdominal,
                txtDobraSubescapular,
                txtDobraAxilarMedia,
                txtDobraCoxa,
                txtDobraToracica,
                txtDobraSuprailiaca,
                txtDobraPanturrilha,
                txtCircPescoco,
                txtCircTorax,
                txtCircOmbro,
                txtCircCintura,
                txtCircQuadril,
                txtCircAbdomen,
                txtCircBracoEsqRelaxado,
                txtCircBracoDirRelaxado,
                txtCircBracoEsqContraido,
                txtCircBracoDirContraido,
                txtCircAntebracoEsq,
                txtCircAntebracoDir,
                txtCircCoxaEsqProximal,
                txtCircCoxaDirProximal,
                txtCircCoxaEsqMedial,
                txtCircCoxaDirMedial,
                txtCircCoxaEsqDistal,
                txtCircCoxaDirDistal,
                txtCircPanturrilhaEsq,
                txtCircPanturrilhaDir,
                txtObservacoesGerais,
                txtObservacoesNutricionais,
                txtAnotacoesProfissional
        );
    }

    private void configurarMascarasNumericas() {
        maskManager.configurarMascarasNumericas(
                montarCamposDecimais(),
                txtIdade
        );
    }

    private List<TextField> montarCamposDecimais() {
        return Arrays.asList(
                txtPeso,
                txtAltura,
                txtDobraTricipital,
                txtDobraBicipital,
                txtDobraAbdominal,
                txtDobraSubescapular,
                txtDobraAxilarMedia,
                txtDobraCoxa,
                txtDobraToracica,
                txtDobraSuprailiaca,
                txtDobraPanturrilha,
                txtCircPescoco,
                txtCircTorax,
                txtCircOmbro,
                txtCircCintura,
                txtCircQuadril,
                txtCircAbdomen,
                txtCircBracoEsqRelaxado,
                txtCircBracoDirRelaxado,
                txtCircBracoEsqContraido,
                txtCircBracoDirContraido,
                txtCircAntebracoEsq,
                txtCircAntebracoDir,
                txtCircCoxaEsqProximal,
                txtCircCoxaDirProximal,
                txtCircCoxaEsqMedial,
                txtCircCoxaDirMedial,
                txtCircCoxaEsqDistal,
                txtCircCoxaDirDistal,
                txtCircPanturrilhaEsq,
                txtCircPanturrilhaDir
        );
    }

    private void configurarEventos() {
        if (cbPacientes != null) {
            cbPacientes.valueProperty().addListener((obs, antigo, novo) -> {
                if (novo != null) {
                    comboManager.preencherIdadePorPaciente(novo, txtIdade);
                    carregarHistoricoPaciente();
                }
            });
        }

        adicionarListenerCalculo(txtPeso);
        adicionarListenerCalculo(txtAltura);
        adicionarListenerCalculo(txtIdade);

        if (cbSexo != null) {
            cbSexo.valueProperty().addListener((obs, antigo, novo) -> atualizarPreviewCalculos());
        }

        adicionarListenerCalculo(txtDobraTricipital);
        adicionarListenerCalculo(txtDobraBicipital);
        adicionarListenerCalculo(txtDobraAbdominal);
        adicionarListenerCalculo(txtDobraSubescapular);
        adicionarListenerCalculo(txtDobraAxilarMedia);
        adicionarListenerCalculo(txtDobraCoxa);
        adicionarListenerCalculo(txtDobraToracica);
        adicionarListenerCalculo(txtDobraSuprailiaca);
        adicionarListenerCalculo(txtDobraPanturrilha);
        adicionarListenerCalculo(txtCircCintura);
        adicionarListenerCalculo(txtCircQuadril);
    }

    private void adicionarListenerCalculo(TextField campo) {
        if (campo != null) {
            campo.textProperty().addListener((obs, antigo, novo) -> atualizarPreviewCalculos());
        }
    }

    private void novaAvaliacao() {
        formManager.novaAvaliacao();
        resultManager.limparResultados(
                lblImc,
                lblClassificacaoImc,
                lblPercentualGordura,
                lblProtocolo,
                lblMassaMagra,
                lblMassaGorda,
                lblRcq,
                lblClassificacaoRcq
        );
        selectionManager.limparSelecao(tableAvaliacoes);
    }

    private void preencherFormularioSelecionado(AvaliacaoFisica avaliacao) {
        formManager.preencherFormulario(avaliacao);
        resultManager.atualizarLabelsResultado(
                avaliacao,
                lblImc,
                lblClassificacaoImc,
                lblPercentualGordura,
                lblProtocolo,
                lblMassaMagra,
                lblMassaGorda,
                lblRcq,
                lblClassificacaoRcq
        );
    }

    private void atualizarPreviewCalculos() {
        try {
            AvaliacaoFisica preview = formManager.montarAvaliacaoDoFormulario();
            resultManager.atualizarPreview(
                    preview,
                    lblImc,
                    lblClassificacaoImc,
                    lblPercentualGordura,
                    lblProtocolo,
                    lblMassaMagra,
                    lblMassaGorda,
                    lblRcq,
                    lblClassificacaoRcq
            );
        } catch (Exception ignored) {
            resultManager.limparResultados(
                    lblImc,
                    lblClassificacaoImc,
                    lblPercentualGordura,
                    lblProtocolo,
                    lblMassaMagra,
                    lblMassaGorda,
                    lblRcq,
                    lblClassificacaoRcq
            );
        }
    }

    private void carregarHistoricoPaciente() {
        historyManager.carregarHistoricoPaciente(cbPacientes, tableAvaliacoes, chartPeso);
    }
}
