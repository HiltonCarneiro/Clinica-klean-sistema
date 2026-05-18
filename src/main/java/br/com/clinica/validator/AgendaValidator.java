package br.com.clinica.validator;

import br.com.clinica.dto.AgendaFormData;
import br.com.clinica.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalTime;

public class AgendaValidator {

    private static final LocalTime HORA_ABERTURA = LocalTime.of(7, 0);
    private static final LocalTime HORA_FECHAMENTO = LocalTime.of(19, 0);
    private static final LocalTime ALMOCO_INICIO = LocalTime.of(12, 0);
    private static final LocalTime ALMOCO_FIM = LocalTime.of(13, 0);

    public void validarParaSalvar(AgendaFormData data) {

        if (data == null) {
            throw new BusinessException("Dados do agendamento inválidos.");
        }

        if (data.getData() == null) {
            throw new BusinessException("Selecione uma data.");
        }

        if (data.getData().isBefore(LocalDate.now())) {
            throw new BusinessException("Não é permitido agendar no passado.");
        }

        if (data.getProfissional() == null) {
            throw new BusinessException("Selecione um profissional.");
        }

        if (data.getSala() == null) {
            throw new BusinessException("Selecione uma sala.");
        }

        if (data.getHoraInicio() == null || data.getHoraFim() == null) {
            throw new BusinessException("Informe horários válidos no formato HH:mm.");
        }

        if (!data.getHoraFim().isAfter(data.getHoraInicio())) {
            throw new BusinessException("Hora fim deve ser maior que hora início.");
        }

        if (!horarioPermitido(data.getHoraInicio(), data.getHoraFim())) {
            throw new BusinessException("Horário inválido: fora do expediente ou durante almoço (12:00-13:00).");
        }

        if (isBlank(data.getProcedimento())) {
            throw new BusinessException("Informe o procedimento.");
        }
    }

    private boolean horarioPermitido(LocalTime inicio, LocalTime fim) {

        if (inicio.isBefore(HORA_ABERTURA) || fim.isAfter(HORA_FECHAMENTO)) {
            return false;
        }

        boolean sobrepoeAlmoco = !(fim.isBefore(ALMOCO_INICIO) || inicio.isAfter(ALMOCO_FIM));

        return !sobrepoeAlmoco;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}