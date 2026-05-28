package br.com.clinica.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvaliacaoFisica {

    private Long id;
    private Long pacienteId;
    private Paciente paciente;

    private LocalDate dataAvaliacao;
    private String profissionalResponsavel;
    private String sexoBiologico;
    private Integer idadeNoMomento;

    private Double pesoKg;
    private Double alturaM;
    private String objetivoPaciente;
    private String nivelAtividadeFisica;

    private Double dobraTricipitalMm;
    private Double dobraBicipitalMm;
    private Double dobraAbdominalMm;
    private Double dobraSubescapularMm;
    private Double dobraAxilarMediaMm;
    private Double dobraCoxaMm;
    private Double dobraToracicaMm;
    private Double dobraSuprailiacaMm;
    private Double dobraPanturrilhaMm;

    private Double circPescocoCm;
    private Double circToraxCm;
    private Double circOmbroCm;
    private Double circCinturaCm;
    private Double circQuadrilCm;
    private Double circAbdomenCm;

    private Double circBracoEsqRelaxadoCm;
    private Double circBracoDirRelaxadoCm;
    private Double circBracoEsqContraidoCm;
    private Double circBracoDirContraidoCm;

    private Double circAntebracoEsqCm;
    private Double circAntebracoDirCm;

    private Double circCoxaEsqProximalCm;
    private Double circCoxaDirProximalCm;
    private Double circCoxaEsqMedialCm;
    private Double circCoxaDirMedialCm;
    private Double circCoxaEsqDistalCm;
    private Double circCoxaDirDistalCm;

    private Double circPanturrilhaEsqCm;
    private Double circPanturrilhaDirCm;

    private Double imc;
    private String classificacaoImc;
    private String interpretacaoImc;

    private Double percentualGordura;
    private String protocoloGordura;
    private Double massaGordaKg;
    private Double massaMagraKg;

    private Double rcq;
    private String classificacaoRcq;

    private String observacoesGerais;
    private String observacoesNutricionais;
    private String anotacoesProfissional;

    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public AvaliacaoFisica() {
    }

    public String getPacienteNome() {
        return paciente == null ? "" : paciente.getNome();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(Long pacienteId) {
        this.pacienteId = pacienteId;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        this.paciente = paciente;
        if (paciente != null) {
            this.pacienteId = paciente.getId();
        }
    }

    public LocalDate getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(LocalDate dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    public String getProfissionalResponsavel() {
        return profissionalResponsavel;
    }

    public void setProfissionalResponsavel(String profissionalResponsavel) {
        this.profissionalResponsavel = profissionalResponsavel;
    }

    public String getSexoBiologico() {
        return sexoBiologico;
    }

    public void setSexoBiologico(String sexoBiologico) {
        this.sexoBiologico = sexoBiologico;
    }

    public Integer getIdadeNoMomento() {
        return idadeNoMomento;
    }

    public void setIdadeNoMomento(Integer idadeNoMomento) {
        this.idadeNoMomento = idadeNoMomento;
    }

    public Double getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Double pesoKg) {
        this.pesoKg = pesoKg;
    }

    public Double getAlturaM() {
        return alturaM;
    }

    public void setAlturaM(Double alturaM) {
        this.alturaM = alturaM;
    }

    public String getObjetivoPaciente() {
        return objetivoPaciente;
    }

    public void setObjetivoPaciente(String objetivoPaciente) {
        this.objetivoPaciente = objetivoPaciente;
    }

    public String getNivelAtividadeFisica() {
        return nivelAtividadeFisica;
    }

    public void setNivelAtividadeFisica(String nivelAtividadeFisica) {
        this.nivelAtividadeFisica = nivelAtividadeFisica;
    }

    public Double getDobraTricipitalMm() {
        return dobraTricipitalMm;
    }

    public void setDobraTricipitalMm(Double dobraTricipitalMm) {
        this.dobraTricipitalMm = dobraTricipitalMm;
    }

    public Double getDobraBicipitalMm() {
        return dobraBicipitalMm;
    }

    public void setDobraBicipitalMm(Double dobraBicipitalMm) {
        this.dobraBicipitalMm = dobraBicipitalMm;
    }

    public Double getDobraAbdominalMm() {
        return dobraAbdominalMm;
    }

    public void setDobraAbdominalMm(Double dobraAbdominalMm) {
        this.dobraAbdominalMm = dobraAbdominalMm;
    }

    public Double getDobraSubescapularMm() {
        return dobraSubescapularMm;
    }

    public void setDobraSubescapularMm(Double dobraSubescapularMm) {
        this.dobraSubescapularMm = dobraSubescapularMm;
    }

    public Double getDobraAxilarMediaMm() {
        return dobraAxilarMediaMm;
    }

    public void setDobraAxilarMediaMm(Double dobraAxilarMediaMm) {
        this.dobraAxilarMediaMm = dobraAxilarMediaMm;
    }

    public Double getDobraCoxaMm() {
        return dobraCoxaMm;
    }

    public void setDobraCoxaMm(Double dobraCoxaMm) {
        this.dobraCoxaMm = dobraCoxaMm;
    }

    public Double getDobraToracicaMm() {
        return dobraToracicaMm;
    }

    public void setDobraToracicaMm(Double dobraToracicaMm) {
        this.dobraToracicaMm = dobraToracicaMm;
    }

    public Double getDobraSuprailiacaMm() {
        return dobraSuprailiacaMm;
    }

    public void setDobraSuprailiacaMm(Double dobraSuprailiacaMm) {
        this.dobraSuprailiacaMm = dobraSuprailiacaMm;
    }

    public Double getDobraPanturrilhaMm() {
        return dobraPanturrilhaMm;
    }

    public void setDobraPanturrilhaMm(Double dobraPanturrilhaMm) {
        this.dobraPanturrilhaMm = dobraPanturrilhaMm;
    }

    public Double getCircPescocoCm() {
        return circPescocoCm;
    }

    public void setCircPescocoCm(Double circPescocoCm) {
        this.circPescocoCm = circPescocoCm;
    }

    public Double getCircToraxCm() {
        return circToraxCm;
    }

    public void setCircToraxCm(Double circToraxCm) {
        this.circToraxCm = circToraxCm;
    }

    public Double getCircOmbroCm() {
        return circOmbroCm;
    }

    public void setCircOmbroCm(Double circOmbroCm) {
        this.circOmbroCm = circOmbroCm;
    }

    public Double getCircCinturaCm() {
        return circCinturaCm;
    }

    public void setCircCinturaCm(Double circCinturaCm) {
        this.circCinturaCm = circCinturaCm;
    }

    public Double getCircQuadrilCm() {
        return circQuadrilCm;
    }

    public void setCircQuadrilCm(Double circQuadrilCm) {
        this.circQuadrilCm = circQuadrilCm;
    }

    public Double getCircAbdomenCm() {
        return circAbdomenCm;
    }

    public void setCircAbdomenCm(Double circAbdomenCm) {
        this.circAbdomenCm = circAbdomenCm;
    }

    public Double getCircBracoEsqRelaxadoCm() {
        return circBracoEsqRelaxadoCm;
    }

    public void setCircBracoEsqRelaxadoCm(Double circBracoEsqRelaxadoCm) {
        this.circBracoEsqRelaxadoCm = circBracoEsqRelaxadoCm;
    }

    public Double getCircBracoDirRelaxadoCm() {
        return circBracoDirRelaxadoCm;
    }

    public void setCircBracoDirRelaxadoCm(Double circBracoDirRelaxadoCm) {
        this.circBracoDirRelaxadoCm = circBracoDirRelaxadoCm;
    }

    public Double getCircBracoEsqContraidoCm() {
        return circBracoEsqContraidoCm;
    }

    public void setCircBracoEsqContraidoCm(Double circBracoEsqContraidoCm) {
        this.circBracoEsqContraidoCm = circBracoEsqContraidoCm;
    }

    public Double getCircBracoDirContraidoCm() {
        return circBracoDirContraidoCm;
    }

    public void setCircBracoDirContraidoCm(Double circBracoDirContraidoCm) {
        this.circBracoDirContraidoCm = circBracoDirContraidoCm;
    }

    public Double getCircAntebracoEsqCm() {
        return circAntebracoEsqCm;
    }

    public void setCircAntebracoEsqCm(Double circAntebracoEsqCm) {
        this.circAntebracoEsqCm = circAntebracoEsqCm;
    }

    public Double getCircAntebracoDirCm() {
        return circAntebracoDirCm;
    }

    public void setCircAntebracoDirCm(Double circAntebracoDirCm) {
        this.circAntebracoDirCm = circAntebracoDirCm;
    }

    public Double getCircCoxaEsqProximalCm() {
        return circCoxaEsqProximalCm;
    }

    public void setCircCoxaEsqProximalCm(Double circCoxaEsqProximalCm) {
        this.circCoxaEsqProximalCm = circCoxaEsqProximalCm;
    }

    public Double getCircCoxaDirProximalCm() {
        return circCoxaDirProximalCm;
    }

    public void setCircCoxaDirProximalCm(Double circCoxaDirProximalCm) {
        this.circCoxaDirProximalCm = circCoxaDirProximalCm;
    }

    public Double getCircCoxaEsqMedialCm() {
        return circCoxaEsqMedialCm;
    }

    public void setCircCoxaEsqMedialCm(Double circCoxaEsqMedialCm) {
        this.circCoxaEsqMedialCm = circCoxaEsqMedialCm;
    }

    public Double getCircCoxaDirMedialCm() {
        return circCoxaDirMedialCm;
    }

    public void setCircCoxaDirMedialCm(Double circCoxaDirMedialCm) {
        this.circCoxaDirMedialCm = circCoxaDirMedialCm;
    }

    public Double getCircCoxaEsqDistalCm() {
        return circCoxaEsqDistalCm;
    }

    public void setCircCoxaEsqDistalCm(Double circCoxaEsqDistalCm) {
        this.circCoxaEsqDistalCm = circCoxaEsqDistalCm;
    }

    public Double getCircCoxaDirDistalCm() {
        return circCoxaDirDistalCm;
    }

    public void setCircCoxaDirDistalCm(Double circCoxaDirDistalCm) {
        this.circCoxaDirDistalCm = circCoxaDirDistalCm;
    }

    public Double getCircPanturrilhaEsqCm() {
        return circPanturrilhaEsqCm;
    }

    public void setCircPanturrilhaEsqCm(Double circPanturrilhaEsqCm) {
        this.circPanturrilhaEsqCm = circPanturrilhaEsqCm;
    }

    public Double getCircPanturrilhaDirCm() {
        return circPanturrilhaDirCm;
    }

    public void setCircPanturrilhaDirCm(Double circPanturrilhaDirCm) {
        this.circPanturrilhaDirCm = circPanturrilhaDirCm;
    }

    public Double getImc() {
        return imc;
    }

    public void setImc(Double imc) {
        this.imc = imc;
    }

    public String getClassificacaoImc() {
        return classificacaoImc;
    }

    public void setClassificacaoImc(String classificacaoImc) {
        this.classificacaoImc = classificacaoImc;
    }

    public String getInterpretacaoImc() {
        return interpretacaoImc;
    }

    public void setInterpretacaoImc(String interpretacaoImc) {
        this.interpretacaoImc = interpretacaoImc;
    }

    public Double getPercentualGordura() {
        return percentualGordura;
    }

    public void setPercentualGordura(Double percentualGordura) {
        this.percentualGordura = percentualGordura;
    }

    public String getProtocoloGordura() {
        return protocoloGordura;
    }

    public void setProtocoloGordura(String protocoloGordura) {
        this.protocoloGordura = protocoloGordura;
    }

    public Double getMassaGordaKg() {
        return massaGordaKg;
    }

    public void setMassaGordaKg(Double massaGordaKg) {
        this.massaGordaKg = massaGordaKg;
    }

    public Double getMassaMagraKg() {
        return massaMagraKg;
    }

    public void setMassaMagraKg(Double massaMagraKg) {
        this.massaMagraKg = massaMagraKg;
    }

    public Double getRcq() {
        return rcq;
    }

    public void setRcq(Double rcq) {
        this.rcq = rcq;
    }

    public String getClassificacaoRcq() {
        return classificacaoRcq;
    }

    public void setClassificacaoRcq(String classificacaoRcq) {
        this.classificacaoRcq = classificacaoRcq;
    }

    public String getObservacoesGerais() {
        return observacoesGerais;
    }

    public void setObservacoesGerais(String observacoesGerais) {
        this.observacoesGerais = observacoesGerais;
    }

    public String getObservacoesNutricionais() {
        return observacoesNutricionais;
    }

    public void setObservacoesNutricionais(String observacoesNutricionais) {
        this.observacoesNutricionais = observacoesNutricionais;
    }

    public String getAnotacoesProfissional() {
        return anotacoesProfissional;
    }

    public void setAnotacoesProfissional(String anotacoesProfissional) {
        this.anotacoesProfissional = anotacoesProfissional;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}