package br.com.clinica.service;

import br.com.clinica.model.AvaliacaoFisica;

public class AvaliacaoFisicaCalculoService {

    public void calcularResultados(AvaliacaoFisica avaliacao) {
        calcularImc(avaliacao);
        calcularPercentualGordura(avaliacao);
        calcularMassas(avaliacao);
        calcularRcq(avaliacao);
    }

    private void calcularImc(AvaliacaoFisica a) {
        if (a.getPesoKg() == null || a.getAlturaM() == null || a.getAlturaM() <= 0) {
            a.setImc(null);
            a.setClassificacaoImc(null);
            a.setInterpretacaoImc(null);
            return;
        }

        double imc = a.getPesoKg() / Math.pow(a.getAlturaM(), 2);
        imc = arredondar(imc);

        a.setImc(imc);
        a.setClassificacaoImc(classificarImc(imc));
        a.setInterpretacaoImc(interpretarImc(imc));
    }

    private String classificarImc(double imc) {
        if (imc < 18.5) return "Abaixo do peso";
        if (imc < 25) return "Peso adequado";
        if (imc < 30) return "Sobrepeso";
        if (imc < 35) return "Obesidade grau I";
        if (imc < 40) return "Obesidade grau II";
        return "Obesidade grau III";
    }

    private String interpretarImc(double imc) {
        if (imc < 18.5) {
            return "O IMC está abaixo da faixa considerada adequada. Avalie o contexto clínico e nutricional do paciente.";
        }
        if (imc < 25) {
            return "O IMC está dentro da faixa considerada adequada para adultos.";
        }
        if (imc < 30) {
            return "O IMC indica sobrepeso. Recomenda-se acompanhar composição corporal e hábitos de vida.";
        }
        return "O IMC indica obesidade. Recomenda-se avaliação profissional detalhada e acompanhamento contínuo.";
    }

    private void calcularPercentualGordura(AvaliacaoFisica a) {
        if (a.getSexoBiologico() == null || a.getIdadeNoMomento() == null) {
            a.setPercentualGordura(null);
            a.setProtocoloGordura("Não calculado: informe sexo biológico e idade.");
            return;
        }

        String sexo = a.getSexoBiologico().trim().toLowerCase();

        boolean homem = sexo.startsWith("m") || sexo.contains("homem");
        boolean mulher = sexo.startsWith("f") || sexo.contains("mulher");

        if (!homem && !mulher) {
            a.setPercentualGordura(null);
            a.setProtocoloGordura("Não calculado: sexo biológico inválido.");
            return;
        }

        Double soma7 = soma7Dobras(a);

        if (soma7 == null || soma7 <= 0) {
            a.setPercentualGordura(null);
            a.setProtocoloGordura("Não calculado: preencha as 7 dobras do protocolo Jackson & Pollock.");
            return;
        }

        int idade = a.getIdadeNoMomento();
        double densidade;

        if (homem) {
            densidade = 1.112
                    - 0.00043499 * soma7
                    + 0.00000055 * Math.pow(soma7, 2)
                    - 0.00028826 * idade;
        } else {
            densidade = 1.097
                    - 0.00046971 * soma7
                    + 0.00000056 * Math.pow(soma7, 2)
                    - 0.00012828 * idade;
        }

        double percentual = ((4.95 / densidade) - 4.50) * 100;

        a.setPercentualGordura(arredondar(percentual));
        a.setProtocoloGordura("Jackson & Pollock 7 dobras + equação de Siri");
    }

    private Double soma7Dobras(AvaliacaoFisica a) {
        if (a.getDobraToracicaMm() == null) return null;
        if (a.getDobraAxilarMediaMm() == null) return null;
        if (a.getDobraTricipitalMm() == null) return null;
        if (a.getDobraSubescapularMm() == null) return null;
        if (a.getDobraAbdominalMm() == null) return null;
        if (a.getDobraSuprailiacaMm() == null) return null;
        if (a.getDobraCoxaMm() == null) return null;

        return a.getDobraToracicaMm()
                + a.getDobraAxilarMediaMm()
                + a.getDobraTricipitalMm()
                + a.getDobraSubescapularMm()
                + a.getDobraAbdominalMm()
                + a.getDobraSuprailiacaMm()
                + a.getDobraCoxaMm();
    }

    private void calcularMassas(AvaliacaoFisica a) {
        if (a.getPesoKg() == null || a.getPercentualGordura() == null) {
            a.setMassaGordaKg(null);
            a.setMassaMagraKg(null);
            return;
        }

        double massaGorda = a.getPesoKg() * a.getPercentualGordura() / 100.0;
        double massaMagra = a.getPesoKg() - massaGorda;

        a.setMassaGordaKg(arredondar(massaGorda));
        a.setMassaMagraKg(arredondar(massaMagra));
    }

    private void calcularRcq(AvaliacaoFisica a) {
        if (a.getCircCinturaCm() == null || a.getCircQuadrilCm() == null || a.getCircQuadrilCm() <= 0) {
            a.setRcq(null);
            a.setClassificacaoRcq(null);
            return;
        }

        double rcq = a.getCircCinturaCm() / a.getCircQuadrilCm();
        rcq = arredondar(rcq);

        a.setRcq(rcq);
        a.setClassificacaoRcq(classificarRcq(a.getSexoBiologico(), rcq));
    }

    private String classificarRcq(String sexoBiologico, double rcq) {
        if (sexoBiologico == null) return "RCQ calculado. Informe o sexo para classificar risco.";

        String sexo = sexoBiologico.trim().toLowerCase();
        boolean homem = sexo.startsWith("m") || sexo.contains("homem");
        boolean mulher = sexo.startsWith("f") || sexo.contains("mulher");

        if (homem) {
            if (rcq < 0.90) return "Baixo risco";
            if (rcq < 1.00) return "Risco moderado";
            return "Risco elevado";
        }

        if (mulher) {
            if (rcq < 0.80) return "Baixo risco";
            if (rcq < 0.85) return "Risco moderado";
            return "Risco elevado";
        }

        return "RCQ calculado. Sexo não reconhecido para classificação.";
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}