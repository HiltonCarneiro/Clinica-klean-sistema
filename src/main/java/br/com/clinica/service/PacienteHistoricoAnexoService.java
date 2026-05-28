package br.com.clinica.service;

import br.com.clinica.dao.AnexoPacienteDAO;
import br.com.clinica.dao.AnexoPacienteDAO.AnexoPacienteItem;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class PacienteHistoricoAnexoService {

    private final AnexoPacienteDAO anexoDAO = new AnexoPacienteDAO();

    public List<AnexoPacienteItem> listarPorPaciente(Long pacienteId) {
        if (pacienteId == null) {
            return Collections.emptyList();
        }

        return anexoDAO.listarPorPaciente(pacienteId);
    }

    public void abrirAnexo(AnexoPacienteItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Selecione um anexo para abrir.");
        }

        if (item.isNuvem()) {
            anexoDAO.abrirNoNavegadorSignedUrl(item.getStoragePath());
            return;
        }

        File arquivo = item.getFileLegado();

        if (arquivo == null || !arquivo.exists()) {
            throw new IllegalStateException("Arquivo não encontrado. (anexo antigo/local)");
        }

        anexoDAO.abrirNoSistema(arquivo);
    }
}