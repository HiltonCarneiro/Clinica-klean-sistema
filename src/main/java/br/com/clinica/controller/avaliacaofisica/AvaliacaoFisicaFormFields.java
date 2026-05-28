package br.com.clinica.controller.avaliacaofisica;

import br.com.clinica.model.Paciente;
import br.com.clinica.model.Usuario;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AvaliacaoFisicaFormFields {

    public final ComboBox<Paciente> cbPacientes;
    public final ComboBox<Usuario> cbProfissionais;
    public final DatePicker dpDataAvaliacao;
    public final TextField txtObjetivo;
    public final TextField txtPeso;
    public final TextField txtAltura;
    public final ComboBox<String> cbSexo;
    public final TextField txtIdade;
    public final ComboBox<String> cbNivelAtividade;
    public final TextField txtDobraTricipital;
    public final TextField txtDobraBicipital;
    public final TextField txtDobraAbdominal;
    public final TextField txtDobraSubescapular;
    public final TextField txtDobraAxilarMedia;
    public final TextField txtDobraCoxa;
    public final TextField txtDobraToracica;
    public final TextField txtDobraSuprailiaca;
    public final TextField txtDobraPanturrilha;
    public final TextField txtCircPescoco;
    public final TextField txtCircTorax;
    public final TextField txtCircOmbro;
    public final TextField txtCircCintura;
    public final TextField txtCircQuadril;
    public final TextField txtCircAbdomen;
    public final TextField txtCircBracoEsqRelaxado;
    public final TextField txtCircBracoDirRelaxado;
    public final TextField txtCircBracoEsqContraido;
    public final TextField txtCircBracoDirContraido;
    public final TextField txtCircAntebracoEsq;
    public final TextField txtCircAntebracoDir;
    public final TextField txtCircCoxaEsqProximal;
    public final TextField txtCircCoxaDirProximal;
    public final TextField txtCircCoxaEsqMedial;
    public final TextField txtCircCoxaDirMedial;
    public final TextField txtCircCoxaEsqDistal;
    public final TextField txtCircCoxaDirDistal;
    public final TextField txtCircPanturrilhaEsq;
    public final TextField txtCircPanturrilhaDir;
    public final TextArea txtObservacoesGerais;
    public final TextArea txtObservacoesNutricionais;
    public final TextArea txtAnotacoesProfissional;

    public AvaliacaoFisicaFormFields(
            ComboBox<Paciente> cbPacientes,
            ComboBox<Usuario> cbProfissionais,
            DatePicker dpDataAvaliacao,
            TextField txtObjetivo,
            TextField txtPeso,
            TextField txtAltura,
            ComboBox<String> cbSexo,
            TextField txtIdade,
            ComboBox<String> cbNivelAtividade,
            TextField txtDobraTricipital,
            TextField txtDobraBicipital,
            TextField txtDobraAbdominal,
            TextField txtDobraSubescapular,
            TextField txtDobraAxilarMedia,
            TextField txtDobraCoxa,
            TextField txtDobraToracica,
            TextField txtDobraSuprailiaca,
            TextField txtDobraPanturrilha,
            TextField txtCircPescoco,
            TextField txtCircTorax,
            TextField txtCircOmbro,
            TextField txtCircCintura,
            TextField txtCircQuadril,
            TextField txtCircAbdomen,
            TextField txtCircBracoEsqRelaxado,
            TextField txtCircBracoDirRelaxado,
            TextField txtCircBracoEsqContraido,
            TextField txtCircBracoDirContraido,
            TextField txtCircAntebracoEsq,
            TextField txtCircAntebracoDir,
            TextField txtCircCoxaEsqProximal,
            TextField txtCircCoxaDirProximal,
            TextField txtCircCoxaEsqMedial,
            TextField txtCircCoxaDirMedial,
            TextField txtCircCoxaEsqDistal,
            TextField txtCircCoxaDirDistal,
            TextField txtCircPanturrilhaEsq,
            TextField txtCircPanturrilhaDir,
            TextArea txtObservacoesGerais,
            TextArea txtObservacoesNutricionais,
            TextArea txtAnotacoesProfissional
    ) {
        this.cbPacientes = cbPacientes;
        this.cbProfissionais = cbProfissionais;
        this.dpDataAvaliacao = dpDataAvaliacao;
        this.txtObjetivo = txtObjetivo;
        this.txtPeso = txtPeso;
        this.txtAltura = txtAltura;
        this.cbSexo = cbSexo;
        this.txtIdade = txtIdade;
        this.cbNivelAtividade = cbNivelAtividade;
        this.txtDobraTricipital = txtDobraTricipital;
        this.txtDobraBicipital = txtDobraBicipital;
        this.txtDobraAbdominal = txtDobraAbdominal;
        this.txtDobraSubescapular = txtDobraSubescapular;
        this.txtDobraAxilarMedia = txtDobraAxilarMedia;
        this.txtDobraCoxa = txtDobraCoxa;
        this.txtDobraToracica = txtDobraToracica;
        this.txtDobraSuprailiaca = txtDobraSuprailiaca;
        this.txtDobraPanturrilha = txtDobraPanturrilha;
        this.txtCircPescoco = txtCircPescoco;
        this.txtCircTorax = txtCircTorax;
        this.txtCircOmbro = txtCircOmbro;
        this.txtCircCintura = txtCircCintura;
        this.txtCircQuadril = txtCircQuadril;
        this.txtCircAbdomen = txtCircAbdomen;
        this.txtCircBracoEsqRelaxado = txtCircBracoEsqRelaxado;
        this.txtCircBracoDirRelaxado = txtCircBracoDirRelaxado;
        this.txtCircBracoEsqContraido = txtCircBracoEsqContraido;
        this.txtCircBracoDirContraido = txtCircBracoDirContraido;
        this.txtCircAntebracoEsq = txtCircAntebracoEsq;
        this.txtCircAntebracoDir = txtCircAntebracoDir;
        this.txtCircCoxaEsqProximal = txtCircCoxaEsqProximal;
        this.txtCircCoxaDirProximal = txtCircCoxaDirProximal;
        this.txtCircCoxaEsqMedial = txtCircCoxaEsqMedial;
        this.txtCircCoxaDirMedial = txtCircCoxaDirMedial;
        this.txtCircCoxaEsqDistal = txtCircCoxaEsqDistal;
        this.txtCircCoxaDirDistal = txtCircCoxaDirDistal;
        this.txtCircPanturrilhaEsq = txtCircPanturrilhaEsq;
        this.txtCircPanturrilhaDir = txtCircPanturrilhaDir;
        this.txtObservacoesGerais = txtObservacoesGerais;
        this.txtObservacoesNutricionais = txtObservacoesNutricionais;
        this.txtAnotacoesProfissional = txtAnotacoesProfissional;
    }
}
