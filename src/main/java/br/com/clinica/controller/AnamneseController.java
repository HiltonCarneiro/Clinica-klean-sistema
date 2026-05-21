package br.com.clinica.controller;

import br.com.clinica.controller.anamnese.AnamneseFieldFormatter;
import br.com.clinica.controller.anamnese.AnamneseJsonMapper;
import br.com.clinica.controller.anamnese.AnamneseClinicalHistoryManager;
import br.com.clinica.controller.anamnese.AnamneseHistoryManager;
import br.com.clinica.controller.anamnese.AnamneseAttachmentManager;
import br.com.clinica.controller.anamnese.AnamneseFormManager;
import br.com.clinica.controller.anamnese.AnamneseLoadManager;
import br.com.clinica.controller.anamnese.AnamneseEvolutionFileManager;
import br.com.clinica.controller.anamnese.AnamneseSaveManager;
import br.com.clinica.controller.anamnese.AnamneseUiManager;
import br.com.clinica.controller.anamnese.AnamneseInitialSetupManager;
import br.com.clinica.controller.anamnese.AnamneseSelectionManager;
import br.com.clinica.controller.anamnese.AnamneseRefreshManager;
import br.com.clinica.controller.anamnese.AnamneseValidationManager;
import br.com.clinica.controller.anamnese.AnamneseControllerStateManager;
import br.com.clinica.controller.anamnese.AnamneseAttachmentActionManager;
import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AnamneseController {

    // Cabeçalho
    @FXML private Label lblPaciente;
    @FXML private Label lblProfissional;
    @FXML private Label lblAgendamento;
    @FXML private Label lblInfo;
    @FXML private BorderPane rootPane;

    @FXML private ComboBox<String> cbTipo;

    @FXML private Button btnNovaEvolucao;
    @FXML private Button btnSalvarInicial;
    @FXML private Button btnSalvarEvolucao;

    // Principal
    @FXML private TextArea taQueixa;
    @FXML private TextArea taEvolucao;
    @FXML private TextArea taObservacoes;

    // Sinais vitais
    @FXML private TextField tfPA;
    @FXML private TextField tfFC;
    @FXML private TextField tfFR;
    @FXML private TextField tfTemp;
    @FXML private TextField tfPeso;
    @FXML private TextField tfAltura;
    @FXML private TextField tfSpO2;

    // Histórico clínico (textos)
    @FXML private TextArea taAntecedentes;
    @FXML private TextArea taMedicacoes;
    @FXML private TextArea taAlergias;
    @FXML private TextArea taCirurgias;

    // Checklist histórico clínico
    @FXML private CheckBox chkNegaAntecedentes;
    @FXML private CheckBox chkHipertensao;
    @FXML private CheckBox chkDiabetes;
    @FXML private CheckBox chkDislipidemia;
    @FXML private CheckBox chkAsma;
    @FXML private CheckBox chkCardiopatia;
    @FXML private CheckBox chkDoencaRenal;
    @FXML private CheckBox chkDoencaHepatica;
    @FXML private CheckBox chkAutoimune;
    @FXML private CheckBox chkCancer;
    @FXML private CheckBox chkAnsiedadeDepressao;

    @FXML private CheckBox chkSemMedicacoes;
    @FXML private CheckBox chkNegaAlergias;
    @FXML private CheckBox chkNegaCirurgias;

    // Hábitos
    @FXML private ComboBox<String> cbTabagismo;
    @FXML private ComboBox<String> cbAlcool;
    @FXML private TextArea taSono;
    @FXML private TextArea taAtividadeFisica;
    @FXML private TextArea taAlimentacao;

    // Exame físico
    @FXML private TextArea taExameGeral;
    @FXML private TextArea taExameSegmentar;

    // Histórico de registros
    @FXML private TableView<Anamnese> tvHistorico;
    @FXML private TableColumn<Anamnese, String> colData;
    @FXML private TableColumn<Anamnese, String> colTipo;

    // Anexos
    @FXML private TextField tfDescricaoAnexo;
    @FXML private TableView<AnexoPacienteDAO.AnexoPacienteItem> tvAnexos;
    @FXML private TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoData;
    @FXML private TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoArquivo;
    @FXML private TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colAnexoDescricao;
    @FXML private Button btnAnexarPdf;
    @FXML private Button btnAbrirPdf;
    @FXML private Button btnRemoverPdf;

    private final AnamneseDAO anamneseDAO = new AnamneseDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final AnexoPacienteDAO anexoDAO = new AnexoPacienteDAO();
    private final AnamneseFieldFormatter fieldFormatter = new AnamneseFieldFormatter();
    private final AnamneseJsonMapper jsonMapper = new AnamneseJsonMapper();
    private final AnamneseHistoryManager historyManager = new AnamneseHistoryManager();
    private final AnamneseClinicalHistoryManager clinicalHistoryManager = new AnamneseClinicalHistoryManager();
    private final AnamneseAttachmentManager attachmentManager = new AnamneseAttachmentManager(anexoDAO);
    private final AnamneseFormManager formManager = new AnamneseFormManager();
    private final AnamneseLoadManager loadManager = new AnamneseLoadManager();
    private final AnamneseEvolutionFileManager evolutionFileManager = new AnamneseEvolutionFileManager();
    private final AnamneseSaveManager saveManager = new AnamneseSaveManager();
    private final AnamneseUiManager uiManager = new AnamneseUiManager();
    private final AnamneseInitialSetupManager initialSetupManager = new AnamneseInitialSetupManager();
    private final AnamneseSelectionManager selectionManager = new AnamneseSelectionManager();
    private final AnamneseRefreshManager refreshManager = new AnamneseRefreshManager();
    private final AnamneseValidationManager validationManager = new AnamneseValidationManager();
    private final AnamneseControllerStateManager stateManager = new AnamneseControllerStateManager();
    private final AnamneseAttachmentActionManager attachmentActionManager = new AnamneseAttachmentActionManager();

    private Agendamento agendamento;
    private Paciente paciente;

    private Anamnese anamneseInicialAtual;
    private Anamnese selecionada;
    private boolean inicialJaSalva = false;

    @FXML
    public void initialize() {
        initialSetupManager.configurarTelaInicial(
                cbTipo,
                cbTabagismo,
                cbAlcool,
                tfPA,
                tfFC,
                tfFR,
                tfTemp,
                tfPeso,
                tfAltura,
                tfSpO2,
                chkNegaAntecedentes,
                chkHipertensao,
                chkDiabetes,
                chkDislipidemia,
                chkAsma,
                chkCardiopatia,
                chkDoencaRenal,
                chkDoencaHepatica,
                chkAutoimune,
                chkCancer,
                chkAnsiedadeDepressao,
                taAntecedentes,
                chkSemMedicacoes,
                taMedicacoes,
                chkNegaAlergias,
                taAlergias,
                chkNegaCirurgias,
                taCirurgias,
                tvHistorico,
                colData,
                colTipo,
                tvAnexos,
                colAnexoData,
                colAnexoArquivo,
                colAnexoDescricao,
                btnAbrirPdf,
                btnRemoverPdf,
                fieldFormatter,
                clinicalHistoryManager,
                historyManager,
                attachmentManager,
                selecionadaHistorico -> {
                    selecionada = selecionadaHistorico;
                    carregarSelecionada(selecionadaHistorico);
                },
                this::atualizarVisibilidadeBotoes
        );

        atualizarVisibilidadeBotoes(cbTipo.getValue());
    }

    // ✅ ALTERAÇÃO PRINCIPAL: NÃO TRAVA A UI (carrega tudo em background)
    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;

        // UI imediata (sem banco)
        this.paciente = null;
        this.anamneseInicialAtual = null;
        this.selecionada = null;
        this.inicialJaSalva = false;

        stateManager.prepararTrocaDeAgendamento(
                tvHistorico,
                tvAnexos,
                historyManager,
                attachmentManager,
                this::limparFormulario
        );

        preencherCabecalho();
        setInfo("Carregando...");

        // carrega paciente + histórico + anexos em background
        loadManager.carregarTudoAsync(
                agendamento,
                pacienteDAO,
                anamneseDAO,
                anexoDAO,
                resultado -> {
                    paciente = resultado.paciente();
                    anamneseInicialAtual = resultado.anamneseInicial();
                    inicialJaSalva = anamneseInicialAtual != null && anamneseInicialAtual.getId() != null;

                    preencherCabecalho();

                    historyManager.atualizarItens(tvHistorico, resultado.historico());
                    attachmentManager.atualizarItens(tvAnexos, resultado.anexos());

                    cbTipo.getSelectionModel().select(inicialJaSalva ? "EVOLUCAO" : "ANAMNESE_INICIAL");
                    atualizarVisibilidadeBotoes(cbTipo.getValue());

                    setInfo("");
                },
                mensagemErro -> setInfo("Erro ao carregar anamnese: " + mensagemErro)
        );
    }

    private void preencherCabecalho() {
        uiManager.preencherCabecalho(
                lblPaciente,
                lblProfissional,
                lblAgendamento,
                paciente,
                agendamento
        );
    }

    private void atualizarVisibilidadeBotoes(String tipo) {
        uiManager.atualizarVisibilidadeBotoes(
                tipo,
                inicialJaSalva,
                btnSalvarInicial,
                btnSalvarEvolucao,
                btnNovaEvolucao
        );
    }

    @FXML private void onSalvarInicial() { salvar("ANAMNESE_INICIAL"); }
    @FXML private void onSalvarEvolucao() { salvar("EVOLUCAO"); }

    @FXML
    private void onNovaEvolucao() {
        selecionada = null;
        stateManager.prepararNovaEvolucao(
                this::limparFormulario,
                this::setInfo
        );
    }

    private void salvar(String tipo) {
        Usuario usuario = Session.getUsuario();

        AnamneseValidationManager.ValidationResult validacao = validationManager.validarAntesDeSalvar(
                tipo,
                paciente,
                usuario,
                safe(taQueixa),
                safe(taEvolucao)
        );

        if (!validacao.isValido()) {
            setInfo(validacao.getMensagem());
            return;
        }

        fieldFormatter.normalizarDecimaisAntesDeSalvar(
                tfTemp,
                tfPeso,
                tfAltura
        );

        try {
            AnamneseSaveManager.SaveResult resultado = saveManager.salvar(
                    tipo,
                    paciente,
                    agendamento,
                    usuario,
                    selecionada,
                    montarJsonCompleto(),
                    safe(taObservacoes),
                    anamneseDAO,
                    anexoDAO,
                    evolutionFileManager,
                    jsonMapper
            );

            if (resultado.anamneseInicialAtual() != null) {
                anamneseInicialAtual = resultado.anamneseInicialAtual();
                inicialJaSalva = true;
            }

            if (resultado.recarregarHistorico()) {
                carregarInicialEvolucoes();
            }

            if (resultado.limparFormulario()) {
                limparFormulario();
            }

            if (resultado.tipoParaSelecionar() != null) {
                cbTipo.getSelectionModel().select(resultado.tipoParaSelecionar());
                atualizarVisibilidadeBotoes(resultado.tipoParaSelecionar());
            }

            setInfo(resultado.mensagem());
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao salvar: " + e.getMessage());
        }
    }

    // Mantido como orquestração mínima após salvar
    private void carregarInicialEvolucoes() {
        if (paciente == null) {
            return;
        }

        AnamneseRefreshManager.RefreshResult resultado = refreshManager.carregarInicialEvolucoes(
                tvHistorico,
                anamneseDAO,
                historyManager,
                paciente.getId()
        );

        anamneseInicialAtual = resultado.anamneseInicialAtual();
        inicialJaSalva = resultado.inicialJaSalva();
    }

    private void carregarSelecionada(Anamnese anamnese) {
        selectionManager.carregarSelecionada(
                anamnese,
                cbTipo,
                jsonMapper,
                formManager,
                clinicalHistoryManager,
                taQueixa,
                taEvolucao,
                taObservacoes,
                tfPA,
                tfFC,
                tfFR,
                tfTemp,
                tfPeso,
                tfAltura,
                tfSpO2,
                taAntecedentes,
                taMedicacoes,
                taAlergias,
                taCirurgias,
                taSono,
                taAtividadeFisica,
                taAlimentacao,
                taExameGeral,
                taExameSegmentar,
                chkNegaAntecedentes,
                chkHipertensao,
                chkDiabetes,
                chkDislipidemia,
                chkAsma,
                chkCardiopatia,
                chkDoencaRenal,
                chkDoencaHepatica,
                chkAutoimune,
                chkCancer,
                chkAnsiedadeDepressao,
                chkSemMedicacoes,
                chkNegaAlergias,
                chkNegaCirurgias,
                cbTabagismo,
                cbAlcool
        );
    }

    // ===================== JSON =====================

    private String montarJsonCompleto() {
        return formManager.montarJsonCompleto(
                taQueixa,
                taEvolucao,
                tfPA,
                tfFC,
                tfFR,
                tfTemp,
                tfPeso,
                tfAltura,
                tfSpO2,
                taAntecedentes,
                taMedicacoes,
                taAlergias,
                taCirurgias,
                taSono,
                taAtividadeFisica,
                taAlimentacao,
                taExameGeral,
                taExameSegmentar,
                chkNegaAntecedentes,
                chkHipertensao,
                chkDiabetes,
                chkDislipidemia,
                chkAsma,
                chkCardiopatia,
                chkDoencaRenal,
                chkDoencaHepatica,
                chkAutoimune,
                chkCancer,
                chkAnsiedadeDepressao,
                chkSemMedicacoes,
                chkNegaAlergias,
                chkNegaCirurgias,
                cbTabagismo,
                cbAlcool,
                clinicalHistoryManager,
                jsonMapper
        );
    }

    // ===================== ANEXOS =====================

    @FXML
    private void onAnexarPdf() {
        attachmentActionManager.anexarPdf(
                attachmentManager,
                tvAnexos,
                tfDescricaoAnexo,
                btnAnexarPdf,
                paciente,
                agendamento,
                selecionada,
                anamneseInicialAtual,
                this::setInfo
        );
    }

    @FXML
    private void onAbrirPdf() {
        attachmentActionManager.abrirPdf(
                attachmentManager,
                tvAnexos,
                this::setInfo
        );
    }

    @FXML
    private void onRemoverPdf() {
        attachmentActionManager.removerPdf(
                attachmentManager,
                tvAnexos,
                paciente,
                this::setInfo
        );
    }

    // ===================== Utils =====================

    private void setInfo(String s) { lblInfo.setText(s == null ? "" : s); }

    private String safe(TextInputControl c) { return c == null || c.getText() == null ? "" : c.getText().trim(); }

    private void limparFormulario() {
        formManager.limparFormulario(
                taQueixa,
                taEvolucao,
                taObservacoes,
                tfPA,
                tfFC,
                tfFR,
                tfTemp,
                tfPeso,
                tfAltura,
                tfSpO2,
                taAntecedentes,
                taMedicacoes,
                taAlergias,
                taCirurgias,
                chkNegaAntecedentes,
                chkHipertensao,
                chkDiabetes,
                chkDislipidemia,
                chkAsma,
                chkCardiopatia,
                chkDoencaRenal,
                chkDoencaHepatica,
                chkAutoimune,
                chkCancer,
                chkAnsiedadeDepressao,
                chkSemMedicacoes,
                chkNegaAlergias,
                chkNegaCirurgias,
                cbTabagismo,
                cbAlcool,
                taSono,
                taAtividadeFisica,
                taAlimentacao,
                taExameGeral,
                taExameSegmentar,
                clinicalHistoryManager
        );
    }
    @FXML
    private void onFechar() {
        fecharTela();
    }

    @FXML
    private void onVoltar() {
        fecharTela();
    }

    private void fecharTela() {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        stage.close();
    }
}