package br.com.clinica.controller.paciente;

import br.com.clinica.dto.PacienteFormData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.util.function.Function;

public class PacienteFormManager {

    private final TextField txtNome;
    private final TextField txtCpf;
    private final TextField txtRg;
    private final DatePicker dpDataNascimento;
    private final TextField txtIdade;
    private final TextField txtTelefone;

    private final TextField txtRua;
    private final TextField txtNumero;
    private final CheckBox chkSemNumero;
    private final TextField txtComplemento;
    private final TextField txtBairro;
    private final TextField txtCidade;
    private final TextField txtCep;
    private final TextField txtUf;
    private final TextField txtResponsavelLegal;

    private final PacienteDocumentFormatter formatter;
    private final Function<String, String> rgFormatter;

    public PacienteFormManager(
            TextField txtNome,
            TextField txtCpf,
            TextField txtRg,
            DatePicker dpDataNascimento,
            TextField txtIdade,
            TextField txtTelefone,
            TextField txtRua,
            TextField txtNumero,
            CheckBox chkSemNumero,
            TextField txtComplemento,
            TextField txtBairro,
            TextField txtCidade,
            TextField txtCep,
            TextField txtUf,
            TextField txtResponsavelLegal,
            PacienteDocumentFormatter formatter,
            Function<String, String> rgFormatter
    ) {
        this.txtNome = txtNome;
        this.txtCpf = txtCpf;
        this.txtRg = txtRg;
        this.dpDataNascimento = dpDataNascimento;
        this.txtIdade = txtIdade;
        this.txtTelefone = txtTelefone;
        this.txtRua = txtRua;
        this.txtNumero = txtNumero;
        this.chkSemNumero = chkSemNumero;
        this.txtComplemento = txtComplemento;
        this.txtBairro = txtBairro;
        this.txtCidade = txtCidade;
        this.txtCep = txtCep;
        this.txtUf = txtUf;
        this.txtResponsavelLegal = txtResponsavelLegal;
        this.formatter = formatter;
        this.rgFormatter = rgFormatter;
    }

    public PacienteFormData obterDadosFormulario() {
        PacienteFormData data = new PacienteFormData();

        data.setNome(valor(txtNome));
        data.setCpf(digits(valor(txtCpf)));
        data.setRg(digits(valor(txtRg)));
        data.setDataNascimento(dpDataNascimento == null ? null : dpDataNascimento.getValue());
        data.setTelefone(digits(valor(txtTelefone)));

        data.setRua(valor(txtRua));
        data.setNumero(obterNumero());
        data.setComplemento(valor(txtComplemento));
        data.setBairro(valor(txtBairro));
        data.setCidade(valor(txtCidade));
        data.setCep(digits(valor(txtCep)));
        data.setUf(valor(txtUf).toUpperCase());
        data.setResponsavelLegal(valor(txtResponsavelLegal));

        return data;
    }

    public void preencherFormulario(PacienteFormData data) {
        if (data == null) {
            return;
        }

        setText(txtNome, data.getNome());
        setText(txtCpf, formatter.formatCpf(data.getCpf()));
        setText(txtRg, rgFormatter.apply(data.getRg()));
        setText(txtTelefone, formatter.formatTelefone(data.getTelefone()));

        if (dpDataNascimento != null) {
            dpDataNascimento.setValue(data.getDataNascimento());
        }

        setText(txtRua, data.getRua());
        setText(txtNumero, data.getNumero());
        setText(txtComplemento, data.getComplemento());
        setText(txtBairro, data.getBairro());
        setText(txtCidade, data.getCidade());
        setText(txtCep, formatter.formatCep(data.getCep()));
        setText(txtUf, data.getUf());
        setText(txtResponsavelLegal, data.getResponsavelLegal());

        if (chkSemNumero != null) {
            chkSemNumero.setSelected("S/N".equalsIgnoreCase(safe(data.getNumero())));
        }
    }

    public void limparFormulario() {
        clear(txtNome);
        clear(txtCpf);
        clear(txtRg);
        clear(txtTelefone);

        if (dpDataNascimento != null) {
            dpDataNascimento.setValue(null);

            if (dpDataNascimento.getEditor() != null) {
                dpDataNascimento.getEditor().clear();
            }
        }

        clear(txtIdade);
        clear(txtRua);
        clear(txtNumero);
        clear(txtComplemento);
        clear(txtBairro);
        clear(txtCidade);
        clear(txtCep);
        clear(txtUf);
        clear(txtResponsavelLegal);

        if (chkSemNumero != null) {
            chkSemNumero.setSelected(false);
        }
    }

    private String obterNumero() {
        if (chkSemNumero != null && chkSemNumero.isSelected()) {
            return "S/N";
        }

        return valor(txtNumero);
    }

    private String valor(TextField field) {
        return field == null || field.getText() == null
                ? ""
                : field.getText().trim();
    }

    private void setText(TextField field, String value) {
        if (field != null) {
            field.setText(safe(value));
        }
    }

    private void clear(TextField field) {
        if (field != null) {
            field.clear();
        }
    }

    private String digits(String value) {
        return safe(value).replaceAll("\\D", "");
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}