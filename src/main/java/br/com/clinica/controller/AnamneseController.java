package br.com.clinica.controller;

import br.com.clinica.dao.AnamneseDAO;
import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.PacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import br.com.clinica.session.Session;
import javafx.collections.FXCollections;
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

    public void setAgendamento(Agendamento agendamento) {
        this.agendamento = agendamento;

        Integer pacienteIdInt = (agendamento != null) ? agendamento.getPacienteId() : null;
        if (pacienteIdInt != null) {
            this.paciente = pacienteDAO.buscarPorId(Long.valueOf(pacienteIdInt));
        } else {
            this.paciente = null;
        }

        preencherCabecalho();
        carregarInicialEvolucoes();

        cbTipo.getSelectionModel().select(inicialJaSalva ? "EVOLUCAO" : "ANAMNESE_INICIAL");
        atualizarVisibilidadeBotoes(cbTipo.getValue());

        selecionada = null;
        limparFormulario();
        carregarAnexosDoPaciente();
        setInfo("");
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

        // ✅ garante formatação final antes de salvar (se o usuário não saiu do campo)
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
        // Se ainda estiver só números, formata para salvar bonitinho
        if (tfTemp != null) tfTemp.setText(formatarDecimalSeSomenteDigitos(tfTemp.getText(), 1, 2));
        if (tfPeso != null) tfPeso.setText(formatarDecimalSeSomenteDigitos(tfPeso.getText(), 1, 3));
        if (tfAltura != null) tfAltura.setText(formatarDecimalSeSomenteDigitos(tfAltura.getText(), 2, 1));
    }

    private String formatarDecimalSeSomenteDigitos(String texto, int decimais, int maxInteiros) {
        if (texto == null) return "";
        String t = texto.trim();
        if (t.isEmpty()) return "";

        // se já tem vírgula, não mexe
        if (t.contains(",")) return t;

        String digits = t.replaceAll("\\D", "");
        if (digits.isEmpty() || digits.matches("0+")) return "";

        int maxLen = maxInteiros + decimais;
        if (digits.length() > maxLen) digits = digits.substring(0, maxLen);

        return formatarDecimalPorDigitos(digits, decimais);
    }

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
        aplicarPAautomatico(tfPA);               // 12080 -> 120/80
        aplicarSomenteNumeros(tfFC, 3);          // 111
        aplicarSomenteNumeros(tfFR, 3);          // 111
        aplicarSomenteNumeros(tfSpO2, 3);        // 97 / 100

        // ✅ decimal em tempo real, SEM travar:
        // Temperatura: "367" -> "36,7"
        aplicarDecimalTempoReal(tfTemp, 1, 2, false);

        // Peso: "2428" -> "242,8"
        aplicarDecimalTempoReal(tfPeso, 1, 3, false);

        // Altura: quer UX mais natural:
        // 1 dígito => mostra "1" (não 0,01)
        // 2 dígitos => "1,7" (ex: 17 -> 1,7)
        // 3 dígitos => "1,72" (ex: 172 -> 1,72)
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

    /**
     * Decimal em tempo real com UX clínica:
     * - permite apagar até vazio sem “prender” em 0,00
     * - modoAltura=true melhora altura para não virar 0,01 ao digitar 1 dígito
     */
    private void aplicarDecimalTempoReal(TextField tf, int decimais, int maxInteiros, boolean modoAltura) {
        if (tf == null) return;

        tf.setTextFormatter(new TextFormatter<String>(change -> {
            String novoTexto = change.getControlNewText();

            // se apagou tudo, fica vazio
            if (novoTexto == null || novoTexto.isEmpty()) {
                return change;
            }

            String digits = novoTexto.replaceAll("\\D", "");

            // se ficou sem dígitos, vazio
            if (digits.isEmpty() || digits.matches("0+")) {
                change.setText("");
                change.setRange(0, change.getControlText().length());
                change.selectRange(0, 0);
                return change;
            }

            // limita tamanho: inteiros + decimais
            int maxLen = maxInteiros + decimais;
            if (digits.length() > maxLen) digits = digits.substring(0, maxLen);

            // ✅ modo especial para altura:
            // 1 dígito => mostra "1"
            // 2 dígitos => "1,7" (decimais=2, então 2 dígitos vira 1 decimal só pra UX)
            // 3+ => formato normal "1,72"
            if (modoAltura) {
                if (digits.length() == 1) {
                    String formatado = digits;
                    change.setText(formatado);
                    change.setRange(0, change.getControlText().length());
                    change.selectRange(formatado.length(), formatado.length());
                    return change;
                }
                if (digits.length() == 2) {
                    // Ex: 17 => 1,7 (mais natural pra altura)
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
        if (paciente == null) { setInfo("Selecione um paciente."); return; }

        FileChooser fc = new FileChooser();
        fc.setTitle("Selecionar PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        File f = fc.showOpenDialog(btnAnexarPdf.getScene().getWindow());
        if (f == null) return;

        String descricao = safeTrim(tfDescricaoAnexo.getText());
        Integer anamneseId = getContextoAnamneseIdParaAnexo();

        try {
            anexoDAO.anexarPdf(paciente.getId(), anamneseId, f, descricao);
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
        if (sel == null) return;

        try {
            anexoDAO.abrirNoSistema(sel.getFile());
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