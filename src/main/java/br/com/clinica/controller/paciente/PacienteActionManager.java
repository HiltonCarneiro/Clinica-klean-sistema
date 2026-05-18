package br.com.clinica.controller.paciente;

import br.com.clinica.dto.PacienteFormData;
import br.com.clinica.exception.BusinessException;
import br.com.clinica.mapper.PacienteFormMapper;
import br.com.clinica.model.Paciente;
import br.com.clinica.service.PacienteService;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class PacienteActionManager {

    private final PacienteService pacienteService;
    private final PacienteFormMapper pacienteFormMapper;
    private final Supplier<PacienteFormData> formDataSupplier;
    private final Consumer<String> mensagemConsumer;

    public PacienteActionManager(
            PacienteService pacienteService,
            PacienteFormMapper pacienteFormMapper,
            Supplier<PacienteFormData> formDataSupplier,
            Consumer<String> mensagemConsumer
    ) {
        this.pacienteService = pacienteService;
        this.pacienteFormMapper = pacienteFormMapper;
        this.formDataSupplier = formDataSupplier;
        this.mensagemConsumer = mensagemConsumer;
    }

    public Paciente salvar(Paciente pacienteSelecionado) {
        try {
            PacienteFormData formData = formDataSupplier.get();

            if (pacienteSelecionado == null) {
                Paciente novoPaciente = pacienteFormMapper.toEntity(new Paciente(), formData);
                novoPaciente.setAtivo(true);

                pacienteService.salvar(novoPaciente);
                mensagemConsumer.accept("Paciente salvo com sucesso.");

                return novoPaciente;
            }

            pacienteFormMapper.toEntity(pacienteSelecionado, formData);
            pacienteService.atualizar(pacienteSelecionado);

            mensagemConsumer.accept("Paciente atualizado com sucesso.");
            return pacienteSelecionado;

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
            return pacienteSelecionado;
        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao salvar paciente.");
            return pacienteSelecionado;
        }
    }

    public void inativar(Paciente paciente) {
        try {
            pacienteService.inativar(paciente);
            mensagemConsumer.accept("Paciente inativado com sucesso.");

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao inativar paciente.");
        }
    }

    public void ativar(Paciente paciente) {
        try {
            pacienteService.ativar(paciente);
            mensagemConsumer.accept("Paciente ativado com sucesso.");

        } catch (BusinessException e) {
            mensagemConsumer.accept(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mensagemConsumer.accept("Erro ao ativar paciente.");
        }
    }
}