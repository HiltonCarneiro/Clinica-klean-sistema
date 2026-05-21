package br.com.clinica.controller.paciente;

import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.util.function.Consumer;
import java.util.function.Function;

public class PacienteFieldSetupManager {

    public void configurarCampos(
            TextField txtNome,
            TextField txtCpf,
            TextField txtRg,
            TextField txtTelefone,
            TextField txtCep,
            TextField txtUf,
            TextField txtNumero,
            CheckBox chkSemNumero,
            DatePicker dpDataNascimento,
            PacienteDocumentFormatter formatter,
            PacienteDatePickerConfigurer datePickerConfigurer,
            Runnable atualizarIdade,
            Runnable onBuscarPaciente,
            PacienteSearchManager searchManager,
            PacienteCepManager cepManager,
            Consumer<String> mensagemConsumer,
            Function<String, String> rgFormatter
    ) {
        if (txtNome != null) {
            txtNome.setTextFormatter(
                    PacienteTextFormatterFactory.nameFormatter()
            );
        }

        if (txtCpf != null) {
            txtCpf.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            11,
                            formatter::formatCpf
                    )
            );
        }

        if (txtRg != null) {
            txtRg.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            7,
                            rgFormatter
                    )
            );
        }

        if (txtTelefone != null) {
            txtTelefone.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            11,
                            formatter::formatTelefone
                    )
            );
        }

        if (txtCep != null) {
            txtCep.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            8,
                            formatter::formatCep
                    )
            );
        }

        if (txtUf != null) {
            txtUf.setTextFormatter(
                    PacienteTextFormatterFactory.ufFormatter()
            );
        }

        if (txtNumero != null) {
            txtNumero.setTextFormatter(
                    PacienteTextFormatterFactory.digitsFormatter(
                            6,
                            value -> value
                    )
            );
        }

        if (chkSemNumero != null && txtNumero != null) {
            chkSemNumero.selectedProperty().addListener(
                    (obs, oldValue, selected) -> {
                        txtNumero.setDisable(selected);

                        if (selected) {
                            txtNumero.clear();
                        }
                    }
            );
        }

        datePickerConfigurer.configure(
                dpDataNascimento,
                mensagemConsumer,
                atualizarIdade
        );

        if (dpDataNascimento != null) {
            dpDataNascimento.valueProperty().addListener(
                    (obs, oldValue, newValue) -> atualizarIdade.run()
            );
        }

        searchManager.configurarCampoBusca(onBuscarPaciente);
        cepManager.configurarBuscaPorEnter();
    }
}
