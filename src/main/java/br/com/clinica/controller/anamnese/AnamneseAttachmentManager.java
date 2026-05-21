package br.com.clinica.controller.anamnese;

import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Anamnese;
import br.com.clinica.model.Paciente;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public class AnamneseAttachmentManager {

    private final AnexoPacienteDAO anexoDAO;

    public AnamneseAttachmentManager(AnexoPacienteDAO anexoDAO) {
        this.anexoDAO = anexoDAO;
    }

    public void configurarTabela(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colData,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colArquivo,
            TableColumn<AnexoPacienteDAO.AnexoPacienteItem, String> colDescricao,
            Button btnAbrirPdf,
            Button btnRemoverPdf
    ) {
        if (colData != null) {
            colData.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDataHora()))
            );
        }

        if (colArquivo != null) {
            colArquivo.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getNomeArquivo()))
            );
        }

        if (colDescricao != null) {
            colDescricao.setCellValueFactory(c ->
                    new SimpleStringProperty(safe(c.getValue().getDescricao()))
            );
        }

        if (tabela != null) {
            tabela.getSelectionModel()
                    .selectedItemProperty()
                    .addListener((obs, oldValue, selected) ->
                            atualizarBotoes(tabela, btnAbrirPdf, btnRemoverPdf)
                    );
        }

        atualizarBotoes(tabela, btnAbrirPdf, btnRemoverPdf);
    }

    public void anexarPdf(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            TextField tfDescricaoAnexo,
            Button btnAnexarPdf,
            Paciente paciente,
            Agendamento agendamento,
            Anamnese selecionada,
            Anamnese anamneseInicialAtual,
            Consumer<String> mensagemConsumer
    ) {
        Long pacienteId = resolverPacienteId(paciente, agendamento);

        if (pacienteId == null) {
            mensagemConsumer.accept("Selecione um paciente.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar PDF");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        var owner = btnAnexarPdf != null && btnAnexarPdf.getScene() != null
                ? btnAnexarPdf.getScene().getWindow()
                : null;

        File arquivo = fileChooser.showOpenDialog(owner);

        if (arquivo == null) {
            return;
        }

        String descricao = safeTrim(
                tfDescricaoAnexo == null ? "" : tfDescricaoAnexo.getText()
        );

        Integer anamneseId = resolverContextoAnamneseId(
                selecionada,
                anamneseInicialAtual
        );

        try {
            anexoDAO.anexarPdf(
                    pacienteId,
                    anamneseId,
                    arquivo,
                    descricao
            );

            if (tfDescricaoAnexo != null) {
                tfDescricaoAnexo.clear();
            }

            carregarAnexosDoPaciente(
                    tabela,
                    pacienteId,
                    mensagemConsumer
            );

            mensagemConsumer.accept("PDF anexado com sucesso.");

        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao anexar PDF: " + e.getMessage());
        }
    }

    public void abrirPdf(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            Consumer<String> mensagemConsumer
    ) {
        if (tabela == null) {
            mensagemConsumer.accept("Selecione um anexo para abrir.");
            return;
        }

        var selecionado = tabela.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            mensagemConsumer.accept("Selecione um anexo para abrir.");
            return;
        }

        try {
            if (selecionado.isNuvem()) {
                anexoDAO.abrirNoNavegadorSignedUrl(
                        selecionado.getStoragePath()
                );
                return;
            }

            File arquivo = selecionado.getFile();

            if (arquivo == null) {
                mensagemConsumer.accept("Arquivo nulo.");
                return;
            }

            if (!arquivo.exists()) {
                mensagemConsumer.accept(
                        "Arquivo não encontrado: " + arquivo.getAbsolutePath()
                );
                return;
            }

            anexoDAO.abrirNoSistema(arquivo);

        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao abrir: " + e.getMessage());
        }
    }

    public void removerPdf(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            Paciente paciente,
            Consumer<String> mensagemConsumer
    ) {
        if (tabela == null) {
            return;
        }

        var selecionado = tabela.getSelectionModel().getSelectedItem();

        if (selecionado == null) {
            return;
        }

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Remover anexo");
        confirmacao.setHeaderText("Remover arquivo");
        confirmacao.setContentText("Deseja remover o arquivo selecionado?");

        if (confirmacao.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            anexoDAO.remover(selecionado.getId());

            if (paciente != null && paciente.getId() != null) {
                carregarAnexosDoPaciente(
                        tabela,
                        paciente.getId(),
                        mensagemConsumer
                );
            } else {
                atualizarItens(tabela, List.of());
            }

            mensagemConsumer.accept("Arquivo removido.");

        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao remover: " + e.getMessage());
        }
    }

    public void carregarAnexosDoPaciente(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            Long pacienteId,
            Consumer<String> mensagemConsumer
    ) {
        if (pacienteId == null) {
            limpar(tabela);
            return;
        }

        try {
            atualizarItens(
                    tabela,
                    anexoDAO.listarPorPaciente(pacienteId)
            );
        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao carregar anexos: " + e.getMessage());
        }
    }

    public void atualizarItens(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            List<AnexoPacienteDAO.AnexoPacienteItem> anexos
    ) {
        if (tabela == null) {
            return;
        }

        tabela.setItems(
                FXCollections.observableArrayList(
                        anexos == null ? List.of() : anexos
                )
        );
    }

    public void limpar(TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela) {
        if (tabela != null) {
            tabela.setItems(FXCollections.observableArrayList());
        }
    }

    private void atualizarBotoes(
            TableView<AnexoPacienteDAO.AnexoPacienteItem> tabela,
            Button btnAbrirPdf,
            Button btnRemoverPdf
    ) {
        boolean temSelecionado = tabela != null
                && tabela.getSelectionModel().getSelectedItem() != null;

        if (btnAbrirPdf != null) {
            btnAbrirPdf.setDisable(!temSelecionado);
        }

        if (btnRemoverPdf != null) {
            btnRemoverPdf.setDisable(!temSelecionado);
        }
    }

    private Long resolverPacienteId(
            Paciente paciente,
            Agendamento agendamento
    ) {
        if (paciente != null && paciente.getId() != null) {
            return paciente.getId();
        }

        if (agendamento != null && agendamento.getPacienteId() != null) {
            return Long.valueOf(agendamento.getPacienteId());
        }

        return null;
    }

    private Integer resolverContextoAnamneseId(
            Anamnese selecionada,
            Anamnese anamneseInicialAtual
    ) {
        if (selecionada != null && selecionada.getId() != null) {
            return selecionada.getId();
        }

        if (anamneseInicialAtual != null
                && anamneseInicialAtual.getId() != null) {
            return anamneseInicialAtual.getId();
        }

        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
