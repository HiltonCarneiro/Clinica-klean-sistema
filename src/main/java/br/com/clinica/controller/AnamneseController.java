package br.com.clinica.controller;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AnamneseController {

    // Cabeçalho
    @FXML private Label lblPaciente;
    @FXML private Label lblProfissional;
    @FXML private Label lblAgendamento;
    @FXML private Label lblInfo;

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

    private Agendamento agendamento;
    private Paciente paciente;

    private Anamnese anamneseInicialAtual;
    private Anamnese selecionada;
    private boolean inicialJaSalva = false;

    private static final DateTimeFormatter DB_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.forLanguageTag("pt-BR"));

    @FXML
    public void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList("ANAMNESE_INICIAL", "EVOLUCAO"));
        cbTipo.getSelectionModel().select("ANAMNESE_INICIAL");
        cbTipo.valueProperty().addListener((obs, o, n) -> atualizarVisibilidadeBotoes(n));

        cbTabagismo.setItems(FXCollections.observableArrayList("Não", "Sim", "Ex-tabagista"));
        cbAlcool.setItems(FXCollections.observableArrayList("Não", "Social", "Frequente"));
        cbTabagismo.getSelectionModel().select("Não");
        cbAlcool.getSelectionModel().select("Não");

        configurarMascarasSinaisVitais();
        configurarChecksHistorico();

        colData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safeStr(c.getValue().getDataHora())));
        colTipo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safeStr(c.getValue().getTipo())));

        tvHistorico.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                selecionada = sel;
                carregarSelecionada(sel);
            }
        });

        colAnexoData.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safeStr(c.getValue().getDataHora())));
        colAnexoArquivo.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safeStr(c.getValue().getNomeArquivo())));
        colAnexoDescricao.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(safeStr(c.getValue().getDescricao())));

        tvAnexos.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> atualizarBotoesAnexos());
        atualizarBotoesAnexos();

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

        limparFormulario();
        tvHistorico.setItems(FXCollections.observableArrayList());
        tvAnexos.setItems(FXCollections.observableArrayList());
        atualizarBotoesAnexos();

        preencherCabecalho();
        setInfo("Carregando...");

        // carrega paciente + histórico + anexos em background
        carregarTudoAsync();
    }

    private void carregarTudoAsync() {
        final Agendamento ag = this.agendamento;

        Task<Void> task = new Task<>() {
            Paciente pac = null;
            Anamnese inicial = null;
            List<Anamnese> historico = List.of();
            List<AnexoPacienteDAO.AnexoPacienteItem> anexos = List.of();

            @Override
            protected Void call() {
                Integer pacienteIdInt = (ag != null) ? ag.getPacienteId() : null;

                if (pacienteIdInt != null) {
                    pac = pacienteDAO.buscarPorId(Long.valueOf(pacienteIdInt));
                }

                if (pac != null && pac.getId() != null) {
                    inicial = anamneseDAO.buscarInicialPorPaciente(pac.getId());
                    historico = anamneseDAO.listarPorPaciente(pac.getId());
                    anexos = anexoDAO.listarPorPaciente(pac.getId());
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    paciente = pac;
                    anamneseInicialAtual = inicial;
                    inicialJaSalva = (anamneseInicialAtual != null && anamneseInicialAtual.getId() != null);

                    preencherCabecalho();

                    tvHistorico.setItems(FXCollections.observableArrayList(historico));
                    tvAnexos.setItems(FXCollections.observableArrayList(anexos));
                    atualizarBotoesAnexos();

                    cbTipo.getSelectionModel().select(inicialJaSalva ? "EVOLUCAO" : "ANAMNESE_INICIAL");
                    atualizarVisibilidadeBotoes(cbTipo.getValue());

                    setInfo("");
                });
            }

            @Override
            protected void failed() {
                Throwable ex = getException();
                if (ex != null) ex.printStackTrace();
                Platform.runLater(() -> setInfo("Erro ao carregar anamnese: " + (ex != null ? ex.getMessage() : "")));
            }
        };

        Thread t = new Thread(task, "anamnese-load");
        t.setDaemon(true);
        t.start();
    }

    private void preencherCabecalho() {
        lblPaciente.setText("Paciente: " + (paciente != null ? paciente.getNome() : "-"));
        Usuario u = Session.getUsuario();
        lblProfissional.setText("Profissional: " + (u != null ? u.getPessoaNome() : "-"));
        lblAgendamento.setText("Agendamento ID: " + (agendamento != null ? agendamento.getId() : "-"));
    }

    private void atualizarVisibilidadeBotoes(String tipo) {
        boolean inicial = "ANAMNESE_INICIAL".equals(tipo);

        btnSalvarInicial.setVisible(inicial);
        btnSalvarInicial.setManaged(inicial);

        btnSalvarEvolucao.setVisible(!inicial);
        btnSalvarEvolucao.setManaged(!inicial);

        btnNovaEvolucao.setVisible(!inicial);
        btnNovaEvolucao.setManaged(!inicial);

        if (!inicial) {
            btnSalvarEvolucao.setDisable(!inicialJaSalva);
            btnNovaEvolucao.setDisable(!inicialJaSalva);
        }
    }

    @FXML private void onSalvarInicial() { salvar("ANAMNESE_INICIAL"); }
    @FXML private void onSalvarEvolucao() { salvar("EVOLUCAO"); }

    @FXML
    private void onNovaEvolucao() {
        selecionada = null;
        limparFormulario();
        setInfo("Nova evolução pronta para preenchimento.");
    }

    private void salvar(String tipo) {
        if (paciente == null) { setInfo("Paciente não encontrado."); return; }
        Usuario u = Session.getUsuario();
        if (u == null) { setInfo("Usuário não autenticado."); return; }

        String queixa = safe(taQueixa);
        String evolucao = safe(taEvolucao);

        if ("ANAMNESE_INICIAL".equals(tipo) && queixa.isBlank()) {
            setInfo("Na anamnese inicial, preencha pelo menos a queixa principal.");
            return;
        }
        if ("EVOLUCAO".equals(tipo) && queixa.isBlank() && evolucao.isBlank()) {
            setInfo("Na evolução, preencha pelo menos Queixa ou Evolução.");
            return;
        }

        normalizarDecimaisAntesDeSalvar();

        Anamnese a = new Anamnese();
        a.setPacienteId(paciente.getId());
        a.setAgendamentoId(agendamento != null ? agendamento.getId() : null);
        a.setProfissionalId(u.getId());
        a.setDataHora(LocalDateTime.now().format(DB_FMT));
        a.setTipo(tipo);
        a.setDadosJson(montarJsonCompleto());
        a.setObservacoes(safe(taObservacoes));

        try {
            if ("ANAMNESE_INICIAL".equals(tipo)) {
                int id = anamneseDAO.salvarOuAtualizarInicial(a);
                a.setId(id);
                anamneseInicialAtual = a;
                inicialJaSalva = true;

                // recarrega histórico (pode manter sync porque é uma ação do usuário; se quiser, dá pra por async também)
                carregarInicialEvolucoes();

                setInfo("Anamnese inicial salva/atualizada. Evoluções liberadas.");
                cbTipo.getSelectionModel().select("EVOLUCAO");
                atualizarVisibilidadeBotoes("EVOLUCAO");
                return;
            }

            boolean editando = (selecionada != null
                    && "EVOLUCAO".equalsIgnoreCase(selecionada.getTipo())
                    && selecionada.getId() != null);

            if (editando) {
                a.setId(selecionada.getId());
                anamneseDAO.atualizar(a);

                anexoDAO.criarOuAtualizarArquivoEvolucao(paciente.getId(), a.getId(), gerarTextoEvolucaoParaArquivo(a));
                carregarInicialEvolucoes();
                setInfo("Evolução atualizada.");
            } else {
                int id = anamneseDAO.inserir(a);
                a.setId(id);

                anexoDAO.criarOuAtualizarArquivoEvolucao(paciente.getId(), a.getId(), gerarTextoEvolucaoParaArquivo(a));
                carregarInicialEvolucoes();
                limparFormulario();
                setInfo("Evolução salva com sucesso.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao salvar: " + e.getMessage());
        }
    }

    private void normalizarDecimaisAntesDeSalvar() {
        if (tfTemp != null) tfTemp.setText(formatarDecimalSeSomenteDigitos(tfTemp.getText(), 1, 2));
        if (tfPeso != null) tfPeso.setText(formatarDecimalSeSomenteDigitos(tfPeso.getText(), 1, 3));
        if (tfAltura != null) tfAltura.setText(formatarDecimalSeSomenteDigitos(tfAltura.getText(), 2, 1));
    }

    private String formatarDecimalSeSomenteDigitos(String texto, int decimais, int maxInteiros) {
        if (texto == null) return "";
        String t = texto.trim();
        if (t.isEmpty()) return "";

        if (t.contains(",")) return t;

        String digits = t.replaceAll("\\D", "");
        if (digits.isEmpty() || digits.matches("0+")) return "";

        int maxLen = maxInteiros + decimais;
        if (digits.length() > maxLen) digits = digits.substring(0, maxLen);

        return formatarDecimalPorDigitos(digits, decimais);
    }

    // Mantido (usado após salvar)
    private void carregarInicialEvolucoes() {
        if (paciente == null) return;
        anamneseInicialAtual = anamneseDAO.buscarInicialPorPaciente(paciente.getId());
        inicialJaSalva = (anamneseInicialAtual != null && anamneseInicialAtual.getId() != null);

        List<Anamnese> lista = anamneseDAO.listarPorPaciente(paciente.getId());
        tvHistorico.setItems(FXCollections.observableArrayList(lista));
    }

    private void carregarSelecionada(Anamnese a) {
        cbTipo.getSelectionModel().select(a.getTipo());
        String json = a.getDadosJson() == null ? "" : a.getDadosJson();

        taQueixa.setText(extrairCampoJson(json, "queixa"));
        taEvolucao.setText(extrairCampoJson(json, "evolucao"));
        taObservacoes.setText(a.getObservacoes() == null ? "" : a.getObservacoes());

        tfPA.setText(extrairCampoJson(json, "pa"));
        tfFC.setText(extrairCampoJson(json, "fc"));
        tfFR.setText(extrairCampoJson(json, "fr"));
        tfTemp.setText(extrairCampoJson(json, "temp"));
        tfPeso.setText(extrairCampoJson(json, "peso"));
        tfAltura.setText(extrairCampoJson(json, "altura"));
        tfSpO2.setText(extrairCampoJson(json, "spo2"));

        taAntecedentes.setText(extrairCampoJson(json, "antecedentes"));
        taMedicacoes.setText(extrairCampoJson(json, "medicacoes"));
        taAlergias.setText(extrairCampoJson(json, "alergias"));
        taCirurgias.setText(extrairCampoJson(json, "cirurgias"));

        boolean negaAnt = isTrue(extrairCampoJson(json, "antecedentes_nega"));
        chkNegaAntecedentes.setSelected(negaAnt);

        String lista = extrairCampoJson(json, "antecedentes_lista");
        aplicarAntecedentesPorLista(lista);
        setAntecedentesEnabled(!negaAnt);

        boolean semMed = isTrue(extrairCampoJson(json, "med_sem_uso"));
        chkSemMedicacoes.setSelected(semMed);
        taMedicacoes.setDisable(semMed);

        boolean negaAl = isTrue(extrairCampoJson(json, "alergias_nega"));
        chkNegaAlergias.setSelected(negaAl);
        taAlergias.setDisable(negaAl);

        boolean negaCi = isTrue(extrairCampoJson(json, "cirurgias_nega"));
        chkNegaCirurgias.setSelected(negaCi);
        taCirurgias.setDisable(negaCi);

        String tab = extrairCampoJson(json, "tabagismo");
        if (!tab.isBlank()) cbTabagismo.getSelectionModel().select(tab);

        String alc = extrairCampoJson(json, "alcool");
        if (!alc.isBlank()) cbAlcool.getSelectionModel().select(alc);

        taSono.setText(extrairCampoJson(json, "sono"));
        taAtividadeFisica.setText(extrairCampoJson(json, "atividade_fisica"));
        taAlimentacao.setText(extrairCampoJson(json, "alimentacao"));

        taExameGeral.setText(extrairCampoJson(json, "exame_geral"));
        taExameSegmentar.setText(extrairCampoJson(json, "exame_segmentar"));
    }

    // ===================== ✅ MÁSCARAS (todas) =====================

    private void configurarMascarasSinaisVitais() {
        aplicarPAautomatico(tfPA);
        aplicarSomenteNumeros(tfFC, 3);
        aplicarSomenteNumeros(tfFR, 3);
        aplicarSomenteNumeros(tfSpO2, 3);

        aplicarDecimalTempoReal(tfTemp, 1, 2, false);
        aplicarDecimalTempoReal(tfPeso, 1, 3, false);
        aplicarDecimalTempoReal(tfAltura, 2, 1, true);
    }

    private void aplicarSomenteNumeros(TextField tf, int maxDigitos) {
        if (tf == null) return;
        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novo = change.getControlNewText();
            if (novo == null || novo.isEmpty()) return change;

            String digits = novo.replaceAll("\\D", "");
            if (digits.length() > maxDigitos) digits = digits.substring(0, maxDigitos);

            change.setText(digits);
            change.setRange(0, change.getControlText().length());
            change.selectRange(digits.length(), digits.length());
            return change;
        }));
    }

    private void aplicarPAautomatico(TextField tf) {
        if (tf == null) return;

        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto == null || novoTexto.isEmpty()) return change;

            String digits = novoTexto.replaceAll("\\D", "");
            if (digits.isEmpty()) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            if (digits.length() > 6) digits = digits.substring(0, 6);

            String formatado;
            if (digits.length() <= 3) {
                formatado = digits;
            } else if (digits.length() <= 5) {
                String sis = digits.substring(0, digits.length() - 2);
                String dia = digits.substring(digits.length() - 2);
                formatado = sis + "/" + dia;
            } else {
                String sis = digits.substring(0, digits.length() - 3);
                String dia = digits.substring(digits.length() - 3);
                formatado = sis + "/" + dia;
            }

            change.setText(formatado);
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatado.length(), formatado.length());
            return change;
        }));
    }

    private void aplicarDecimalTempoReal(TextField tf, int decimais, int maxInteiros, boolean modoAltura) {
        if (tf == null) return;

        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();

            if (novoTexto == null || novoTexto.isEmpty()) {
                return change;
            }

            String digits = novoTexto.replaceAll("\\D", "");

            if (digits.isEmpty() || digits.matches("0+")) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            int maxLen = maxInteiros + decimais;
            if (digits.length() > maxLen) digits = digits.substring(0, maxLen);

            if (modoAltura) {
                if (digits.length() == 1) {
                    String formatado = digits;
                    change.setText(formatado);
                    change.setRange(0, change.getControlText().length());
                    change.selectRange(formatado.length(), formatado.length());
                    return change;
                }
                if (digits.length() == 2) {
                    String formatado = digits.charAt(0) + "," + digits.charAt(1);
                    change.setText(formatado);
                    change.setRange(0, change.getControlText().length());
                    change.selectRange(formatado.length(), formatado.length());
                    return change;
                }
            }

            String formatado = formatarDecimalPorDigitos(digits, decimais);

            change.setText(formatado);
            change.setRange(0, change.getControlText().length());
            change.selectRange(formatado.length(), formatado.length());
            return change;
        }));
    }

    private String formatarDecimalPorDigitos(String digits, int decimais) {
        if (digits == null || digits.isEmpty()) return "";

        if (digits.length() <= decimais) {
            String zeros = "0".repeat(decimais - digits.length() + 1);
            digits = zeros + digits;
        }

        int split = digits.length() - decimais;
        String inteiro = digits.substring(0, split);
        String decimal = digits.substring(split);

        inteiro = inteiro.replaceFirst("^0+(?!$)", "");
        return inteiro + "," + decimal;
    }

    // ===================== HISTÓRICO CLÍNICO: CHECKS =====================

    private void configurarChecksHistorico() {
        chkNegaAntecedentes.selectedProperty().addListener((obs, o, negou) -> {
            setAntecedentesEnabled(!negou);
            if (negou) {
                desmarcarAntecedentes();
                taAntecedentes.clear();
            }
        });

        chkSemMedicacoes.selectedProperty().addListener((obs, o, semUso) -> {
            taMedicacoes.setDisable(semUso);
            if (semUso) taMedicacoes.clear();
        });

        chkNegaAlergias.selectedProperty().addListener((obs, o, nega) -> {
            taAlergias.setDisable(nega);
            if (nega) taAlergias.clear();
        });

        chkNegaCirurgias.selectedProperty().addListener((obs, o, nega) -> {
            taCirurgias.setDisable(nega);
            if (nega) taCirurgias.clear();
        });
    }

    private void setAntecedentesEnabled(boolean enabled) {
        chkHipertensao.setDisable(!enabled);
        chkDiabetes.setDisable(!enabled);
        chkDislipidemia.setDisable(!enabled);
        chkAsma.setDisable(!enabled);
        chkCardiopatia.setDisable(!enabled);
        chkDoencaRenal.setDisable(!enabled);
        chkDoencaHepatica.setDisable(!enabled);
        chkAutoimune.setDisable(!enabled);
        chkCancer.setDisable(!enabled);
        chkAnsiedadeDepressao.setDisable(!enabled);
        taAntecedentes.setDisable(!enabled);
    }

    private void desmarcarAntecedentes() {
        chkHipertensao.setSelected(false);
        chkDiabetes.setSelected(false);
        chkDislipidemia.setSelected(false);
        chkAsma.setSelected(false);
        chkCardiopatia.setSelected(false);
        chkDoencaRenal.setSelected(false);
        chkDoencaHepatica.setSelected(false);
        chkAutoimune.setSelected(false);
        chkCancer.setSelected(false);
        chkAnsiedadeDepressao.setSelected(false);
    }

    private void aplicarAntecedentesPorLista(String lista) {
        desmarcarAntecedentes();
        if (lista == null || lista.isBlank()) return;

        String[] parts = lista.split(";");
        for (String raw : parts) {
            String item = raw.trim().toLowerCase();
            if (item.isBlank()) continue;

            if (item.contains("hipert")) { chkHipertensao.setSelected(true); continue; }
            if (item.contains("diabet")) { chkDiabetes.setSelected(true); continue; }
            if (item.contains("dislip")) { chkDislipidemia.setSelected(true); continue; }
            if (item.contains("asma")) { chkAsma.setSelected(true); continue; }
            if (item.contains("cardio")) { chkCardiopatia.setSelected(true); continue; }
            if (item.contains("renal")) { chkDoencaRenal.setSelected(true); continue; }
            if (item.contains("hepát") || item.contains("hepat")) { chkDoencaHepatica.setSelected(true); continue; }
            if (item.contains("autoim")) { chkAutoimune.setSelected(true); continue; }
            if (item.contains("cânc") || item.contains("canc")) { chkCancer.setSelected(true); continue; }
            if (item.contains("ansied") || item.contains("depress")) { chkAnsiedadeDepressao.setSelected(true); }
        }
    }

    private String montarListaAntecedentes() {
        if (chkNegaAntecedentes.isSelected()) return "";
        List<String> itens = new ArrayList<>();
        if (chkHipertensao.isSelected()) itens.add("Hipertensão");
        if (chkDiabetes.isSelected()) itens.add("Diabetes");
        if (chkDislipidemia.isSelected()) itens.add("Dislipidemia");
        if (chkAsma.isSelected()) itens.add("Asma");
        if (chkCardiopatia.isSelected()) itens.add("Cardiopatia");
        if (chkDoencaRenal.isSelected()) itens.add("Doença renal");
        if (chkDoencaHepatica.isSelected()) itens.add("Doença hepática");
        if (chkAutoimune.isSelected()) itens.add("Autoimune");
        if (chkCancer.isSelected()) itens.add("Câncer");
        if (chkAnsiedadeDepressao.isSelected()) itens.add("Ansiedade/Depressão");
        return String.join("; ", itens);
    }

    private String montarTextoAntecedentesParaTela() {
        if (chkNegaAntecedentes.isSelected()) return "Nega antecedentes.";
        String lista = montarListaAntecedentes();
        String outros = safe(taAntecedentes);

        if (lista.isBlank() && outros.isBlank()) return "";
        if (!lista.isBlank() && outros.isBlank()) return lista;
        if (lista.isBlank()) return outros;
        return lista + "\nOutros: " + outros;
    }

    // ===================== JSON =====================

    private String montarJsonCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");

        put(sb, "queixa", safe(taQueixa)); sb.append(",");
        put(sb, "evolucao", safe(taEvolucao)); sb.append(",");

        put(sb, "pa", safe(tfPA)); sb.append(",");
        put(sb, "fc", safe(tfFC)); sb.append(",");
        put(sb, "fr", safe(tfFR)); sb.append(",");
        put(sb, "temp", safe(tfTemp)); sb.append(",");
        put(sb, "peso", safe(tfPeso)); sb.append(",");
        put(sb, "altura", safe(tfAltura)); sb.append(",");
        put(sb, "spo2", safe(tfSpO2)); sb.append(",");

        put(sb, "antecedentes", montarTextoAntecedentesParaTela()); sb.append(",");
        put(sb, "antecedentes_lista", montarListaAntecedentes()); sb.append(",");
        put(sb, "antecedentes_nega", chkNegaAntecedentes.isSelected() ? "1" : "0"); sb.append(",");

        put(sb, "medicacoes", safe(taMedicacoes)); sb.append(",");
        put(sb, "med_sem_uso", chkSemMedicacoes.isSelected() ? "1" : "0"); sb.append(",");

        put(sb, "alergias", safe(taAlergias)); sb.append(",");
        put(sb, "alergias_nega", chkNegaAlergias.isSelected() ? "1" : "0"); sb.append(",");

        put(sb, "cirurgias", safe(taCirurgias)); sb.append(",");
        put(sb, "cirurgias_nega", chkNegaCirurgias.isSelected() ? "1" : "0"); sb.append(",");

        put(sb, "tabagismo", cbTabagismo.getValue()); sb.append(",");
        put(sb, "alcool", cbAlcool.getValue()); sb.append(",");
        put(sb, "sono", safe(taSono)); sb.append(",");
        put(sb, "atividade_fisica", safe(taAtividadeFisica)); sb.append(",");
        put(sb, "alimentacao", safe(taAlimentacao)); sb.append(",");

        put(sb, "exame_geral", safe(taExameGeral)); sb.append(",");
        put(sb, "exame_segmentar", safe(taExameSegmentar));

        sb.append("}");
        return sb.toString();
    }

    private void put(StringBuilder sb, String campo, String valor) {
        sb.append("\"").append(campo).append("\":\"").append(escape(valor)).append("\"");
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n");
    }

    private String extrairCampoJson(String json, String campo) {
        if (json == null || json.isBlank()) return "";
        String key = "\"" + campo + "\":\"";
        int i = json.indexOf(key);
        if (i < 0) return "";
        int start = i + key.length();
        int end = json.indexOf("\"", start);
        if (end < 0) return "";
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private boolean isTrue(String v) {
        if (v == null) return false;
        v = v.trim();
        return v.equals("1") || v.equalsIgnoreCase("true") || v.equalsIgnoreCase("sim");
    }

    // ===================== ANEXOS =====================

    private void carregarAnexosDoPaciente() {
        if (paciente == null) return;
        tvAnexos.setItems(FXCollections.observableArrayList(anexoDAO.listarPorPaciente(paciente.getId())));
        atualizarBotoesAnexos();
    }

    private void atualizarBotoesAnexos() {
        boolean temSel = tvAnexos.getSelectionModel().getSelectedItem() != null;
        btnAbrirPdf.setDisable(!temSel);
        btnRemoverPdf.setDisable(!temSel);
    }

    private Integer getContextoAnamneseIdParaAnexo() {
        if (selecionada != null && selecionada.getId() != null) return selecionada.getId();
        if (anamneseInicialAtual != null && anamneseInicialAtual.getId() != null) return anamneseInicialAtual.getId();
        return null;
    }

    @FXML
    private void onAnexarPdf() {
        Long pacienteId = null;
        if (paciente != null && paciente.getId() != null) {
            pacienteId = paciente.getId();
        } else if (agendamento != null && agendamento.getPacienteId() != null) {
            pacienteId = Long.valueOf(agendamento.getPacienteId());
        }

        if (pacienteId == null) {
            setInfo("Selecione um paciente.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        var owner = (btnAnexarPdf.getScene() != null) ? btnAnexarPdf.getScene().getWindow() : null;
        File f = fc.showOpenDialog(owner);
        if (f == null) return;

        String descricao = safeTrim(tfDescricaoAnexo.getText());
        Integer anamneseId = getContextoAnamneseIdParaAnexo();

        try {
            anexoDAO.anexarPdf(pacienteId, anamneseId, f, descricao);

            tfDescricaoAnexo.clear();
            carregarAnexosDoPaciente();
            setInfo("PDF anexado com sucesso.");
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao anexar PDF: " + e.getMessage());
        }
    }

    @FXML
    private void onAbrirPdf() {
        var sel = tvAnexos.getSelectionModel().getSelectedItem();
        if (sel == null) {
            setInfo("Selecione um anexo para abrir.");
            return;
        }

        try {
            // NOVO: anexo na nuvem (Supabase Storage) -> abre no navegador (signed URL)
            if (sel.isNuvem()) {
                anexoDAO.abrirNoNavegadorSignedUrl(sel.getStoragePath());
                return;
            }

            // LEGADO: anexo local -> abre no sistema
            File f = sel.getFile(); // compatibilidade
            if (f == null) {
                setInfo("Arquivo nulo.");
                return;
            }
            if (!f.exists()) {
                setInfo("Arquivo não encontrado: " + f.getAbsolutePath());
                return;
            }

            anexoDAO.abrirNoSistema(f);

        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao abrir: " + e.getMessage());
        }
    }

    @FXML
    private void onRemoverPdf() {
        var sel = tvAnexos.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remover anexo");
        confirm.setHeaderText("Remover arquivo");
        confirm.setContentText("Deseja remover o arquivo selecionado?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            anexoDAO.remover(sel.getId());
            carregarAnexosDoPaciente();
            setInfo("Arquivo removido.");
        } catch (Exception e) {
            e.printStackTrace();
            setInfo("Erro ao remover: " + e.getMessage());
        }
    }

    private String gerarTextoEvolucaoParaArquivo(Anamnese a) {
        String json = a.getDadosJson() == null ? "" : a.getDadosJson();
        StringBuilder sb = new StringBuilder();

        sb.append("Data/Hora: ").append(safeStr(a.getDataHora())).append("\n");
        sb.append("Tipo: ").append(safeStr(a.getTipo())).append("\n\n");

        sb.append("Queixa:\n").append(extrairCampoJson(json, "queixa")).append("\n\n");
        sb.append("Evolução:\n").append(extrairCampoJson(json, "evolucao")).append("\n\n");
        sb.append("Observações:\n").append(a.getObservacoes() == null ? "" : a.getObservacoes()).append("\n\n");

        sb.append("Sinais Vitais:\n");
        sb.append("PA: ").append(extrairCampoJson(json, "pa")).append("\n");
        sb.append("FC: ").append(extrairCampoJson(json, "fc")).append("\n");
        sb.append("FR: ").append(extrairCampoJson(json, "fr")).append("\n");
        sb.append("Temp: ").append(extrairCampoJson(json, "temp")).append("\n");
        sb.append("Peso: ").append(extrairCampoJson(json, "peso")).append("\n");
        sb.append("Altura: ").append(extrairCampoJson(json, "altura")).append("\n");
        sb.append("SpO2: ").append(extrairCampoJson(json, "spo2")).append("\n\n");

        return sb.toString();
    }

    // ===================== Utils =====================

    private void setInfo(String s) { lblInfo.setText(s == null ? "" : s); }

    private String safe(TextInputControl c) { return c == null || c.getText() == null ? "" : c.getText().trim(); }

    private String safeTrim(String s) { return s == null ? "" : s.trim(); }

    private String safeStr(String s) { return s == null ? "" : s; }

    private void limparFormulario() {
        taQueixa.clear();
        taEvolucao.clear();
        taObservacoes.clear();

        tfPA.clear();
        tfFC.clear();
        tfFR.clear();
        tfTemp.clear();
        tfPeso.clear();
        tfAltura.clear();
        tfSpO2.clear();

        taAntecedentes.clear();
        taMedicacoes.clear();
        taAlergias.clear();
        taCirurgias.clear();

        chkNegaAntecedentes.setSelected(false);
        desmarcarAntecedentes();
        setAntecedentesEnabled(true);

        chkSemMedicacoes.setSelected(false);
        taMedicacoes.setDisable(false);

        chkNegaAlergias.setSelected(false);
        taAlergias.setDisable(false);

        chkNegaCirurgias.setSelected(false);
        taCirurgias.setDisable(false);

        cbTabagismo.getSelectionModel().select("Não");
        cbAlcool.getSelectionModel().select("Não");
        taSono.clear();
        taAtividadeFisica.clear();
        taAlimentacao.clear();

        taExameGeral.clear();
        taExameSegmentar.clear();
    }
}