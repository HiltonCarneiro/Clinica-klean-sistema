package br.com.clinica.dao;

import br.com.clinica.database.DatabaseConfig;
import br.com.clinica.model.AvaliacaoFisica;
import br.com.clinica.model.Paciente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvaliacaoFisicaDAO {

    private final AuditLogDAO audit = new AuditLogDAO();

    public void salvar(AvaliacaoFisica avaliacao) {
        if (avaliacao.getId() == null) {
            inserir(avaliacao);
        } else {
            atualizar(avaliacao);
        }
    }

    private void inserir(AvaliacaoFisica a) {
        String sql = """
            INSERT INTO avaliacao_fisica (
                paciente_id, data_avaliacao, profissional_responsavel, sexo_biologico, idade_no_momento,
                peso_kg, altura_m, objetivo_paciente, nivel_atividade_fisica,
                dobra_tricipital_mm, dobra_bicipital_mm, dobra_abdominal_mm, dobra_subescapular_mm,
                dobra_axilar_media_mm, dobra_coxa_mm, dobra_toracica_mm, dobra_suprailiaca_mm, dobra_panturrilha_mm,
                circ_pescoco_cm, circ_torax_cm, circ_ombro_cm, circ_cintura_cm, circ_quadril_cm, circ_abdomen_cm,
                circ_braco_esq_relaxado_cm, circ_braco_dir_relaxado_cm,
                circ_braco_esq_contraido_cm, circ_braco_dir_contraido_cm,
                circ_antebraco_esq_cm, circ_antebraco_dir_cm,
                circ_coxa_esq_proximal_cm, circ_coxa_dir_proximal_cm,
                circ_coxa_esq_medial_cm, circ_coxa_dir_medial_cm,
                circ_coxa_esq_distal_cm, circ_coxa_dir_distal_cm,
                circ_panturrilha_esq_cm, circ_panturrilha_dir_cm,
                imc, classificacao_imc, interpretacao_imc,
                percentual_gordura, protocolo_gordura, massa_gorda_kg, massa_magra_kg,
                rcq, classificacao_rcq,
                observacoes_gerais, observacoes_nutricionais, anotacoes_profissional
            ) VALUES (
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?,
                ?, ?, ?
            )
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preencherStatement(ps, a, false);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setId(rs.getLong(1));
                }
            }

            audit.registrarAuto(
                    "CRIAR",
                    "AVALIACAO_FISICA",
                    String.valueOf(a.getId()),
                    "paciente_id=" + a.getPacienteId() + ", data=" + a.getDataAvaliacao()
            );

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar avaliação física.", e);
        }
    }

    private void atualizar(AvaliacaoFisica a) {
        String sql = """
            UPDATE avaliacao_fisica SET
                paciente_id = ?,
                data_avaliacao = ?,
                profissional_responsavel = ?,
                sexo_biologico = ?,
                idade_no_momento = ?,
                peso_kg = ?,
                altura_m = ?,
                objetivo_paciente = ?,
                nivel_atividade_fisica = ?,
                dobra_tricipital_mm = ?,
                dobra_bicipital_mm = ?,
                dobra_abdominal_mm = ?,
                dobra_subescapular_mm = ?,
                dobra_axilar_media_mm = ?,
                dobra_coxa_mm = ?,
                dobra_toracica_mm = ?,
                dobra_suprailiaca_mm = ?,
                dobra_panturrilha_mm = ?,
                circ_pescoco_cm = ?,
                circ_torax_cm = ?,
                circ_ombro_cm = ?,
                circ_cintura_cm = ?,
                circ_quadril_cm = ?,
                circ_abdomen_cm = ?,
                circ_braco_esq_relaxado_cm = ?,
                circ_braco_dir_relaxado_cm = ?,
                circ_braco_esq_contraido_cm = ?,
                circ_braco_dir_contraido_cm = ?,
                circ_antebraco_esq_cm = ?,
                circ_antebraco_dir_cm = ?,
                circ_coxa_esq_proximal_cm = ?,
                circ_coxa_dir_proximal_cm = ?,
                circ_coxa_esq_medial_cm = ?,
                circ_coxa_dir_medial_cm = ?,
                circ_coxa_esq_distal_cm = ?,
                circ_coxa_dir_distal_cm = ?,
                circ_panturrilha_esq_cm = ?,
                circ_panturrilha_dir_cm = ?,
                imc = ?,
                classificacao_imc = ?,
                interpretacao_imc = ?,
                percentual_gordura = ?,
                protocolo_gordura = ?,
                massa_gorda_kg = ?,
                massa_magra_kg = ?,
                rcq = ?,
                classificacao_rcq = ?,
                observacoes_gerais = ?,
                observacoes_nutricionais = ?,
                anotacoes_profissional = ?,
                atualizado_em = NOW()
            WHERE id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            preencherStatement(ps, a, true);
            ps.executeUpdate();

            audit.registrarAuto(
                    "EDITAR",
                    "AVALIACAO_FISICA",
                    String.valueOf(a.getId()),
                    "paciente_id=" + a.getPacienteId() + ", data=" + a.getDataAvaliacao()
            );

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar avaliação física.", e);
        }
    }

    private void preencherStatement(PreparedStatement ps, AvaliacaoFisica a, boolean incluirIdFinal) throws SQLException {
        int i = 1;

        setLong(ps, i++, a.getPacienteId());
        setDate(ps, i++, a.getDataAvaliacao());
        setString(ps, i++, a.getProfissionalResponsavel());
        setString(ps, i++, a.getSexoBiologico());
        setInteger(ps, i++, a.getIdadeNoMomento());

        setDouble(ps, i++, a.getPesoKg());
        setDouble(ps, i++, a.getAlturaM());
        setString(ps, i++, a.getObjetivoPaciente());
        setString(ps, i++, a.getNivelAtividadeFisica());

        setDouble(ps, i++, a.getDobraTricipitalMm());
        setDouble(ps, i++, a.getDobraBicipitalMm());
        setDouble(ps, i++, a.getDobraAbdominalMm());
        setDouble(ps, i++, a.getDobraSubescapularMm());
        setDouble(ps, i++, a.getDobraAxilarMediaMm());
        setDouble(ps, i++, a.getDobraCoxaMm());
        setDouble(ps, i++, a.getDobraToracicaMm());
        setDouble(ps, i++, a.getDobraSuprailiacaMm());
        setDouble(ps, i++, a.getDobraPanturrilhaMm());

        setDouble(ps, i++, a.getCircPescocoCm());
        setDouble(ps, i++, a.getCircToraxCm());
        setDouble(ps, i++, a.getCircOmbroCm());
        setDouble(ps, i++, a.getCircCinturaCm());
        setDouble(ps, i++, a.getCircQuadrilCm());
        setDouble(ps, i++, a.getCircAbdomenCm());

        setDouble(ps, i++, a.getCircBracoEsqRelaxadoCm());
        setDouble(ps, i++, a.getCircBracoDirRelaxadoCm());
        setDouble(ps, i++, a.getCircBracoEsqContraidoCm());
        setDouble(ps, i++, a.getCircBracoDirContraidoCm());

        setDouble(ps, i++, a.getCircAntebracoEsqCm());
        setDouble(ps, i++, a.getCircAntebracoDirCm());

        setDouble(ps, i++, a.getCircCoxaEsqProximalCm());
        setDouble(ps, i++, a.getCircCoxaDirProximalCm());
        setDouble(ps, i++, a.getCircCoxaEsqMedialCm());
        setDouble(ps, i++, a.getCircCoxaDirMedialCm());
        setDouble(ps, i++, a.getCircCoxaEsqDistalCm());
        setDouble(ps, i++, a.getCircCoxaDirDistalCm());

        setDouble(ps, i++, a.getCircPanturrilhaEsqCm());
        setDouble(ps, i++, a.getCircPanturrilhaDirCm());

        setDouble(ps, i++, a.getImc());
        setString(ps, i++, a.getClassificacaoImc());
        setString(ps, i++, a.getInterpretacaoImc());

        setDouble(ps, i++, a.getPercentualGordura());
        setString(ps, i++, a.getProtocoloGordura());
        setDouble(ps, i++, a.getMassaGordaKg());
        setDouble(ps, i++, a.getMassaMagraKg());

        setDouble(ps, i++, a.getRcq());
        setString(ps, i++, a.getClassificacaoRcq());

        setString(ps, i++, a.getObservacoesGerais());
        setString(ps, i++, a.getObservacoesNutricionais());
        setString(ps, i++, a.getAnotacoesProfissional());

        if (incluirIdFinal) {
            setLong(ps, i, a.getId());
        }
    }

    public AvaliacaoFisica buscarPorId(Long id) {
        String sql = """
            SELECT af.*, p.nome AS paciente_nome, p.cpf AS paciente_cpf, p.telefone AS paciente_telefone
              FROM avaliacao_fisica af
              JOIN paciente p ON p.id = af.paciente_id
             WHERE af.id = ?
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar avaliação física.", e);
        }

        return null;
    }

    public List<AvaliacaoFisica> listarPorPaciente(Long pacienteId) {
        String sql = """
            SELECT af.*, p.nome AS paciente_nome, p.cpf AS paciente_cpf, p.telefone AS paciente_telefone
              FROM avaliacao_fisica af
              JOIN paciente p ON p.id = af.paciente_id
             WHERE af.paciente_id = ?
             ORDER BY af.data_avaliacao DESC, af.id DESC
            """;

        List<AvaliacaoFisica> lista = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapRow(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar avaliações físicas do paciente.", e);
        }

        return lista;
    }

    public AvaliacaoFisica buscarUltimaPorPaciente(Long pacienteId) {
        String sql = """
            SELECT af.*, p.nome AS paciente_nome, p.cpf AS paciente_cpf, p.telefone AS paciente_telefone
              FROM avaliacao_fisica af
              JOIN paciente p ON p.id = af.paciente_id
             WHERE af.paciente_id = ?
             ORDER BY af.data_avaliacao DESC, af.id DESC
             LIMIT 1
            """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, pacienteId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar última avaliação física.", e);
        }

        return null;
    }

    public void excluir(Long id) {
        String sql = "DELETE FROM avaliacao_fisica WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

            audit.registrarAuto(
                    "EXCLUIR",
                    "AVALIACAO_FISICA",
                    String.valueOf(id),
                    "Avaliação física removida."
            );

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir avaliação física.", e);
        }
    }

    private AvaliacaoFisica mapRow(ResultSet rs) throws SQLException {
        AvaliacaoFisica a = new AvaliacaoFisica();

        a.setId(rs.getLong("id"));
        a.setPacienteId(rs.getLong("paciente_id"));

        Paciente paciente = new Paciente();
        paciente.setId(rs.getLong("paciente_id"));
        paciente.setNome(rs.getString("paciente_nome"));
        paciente.setCpf(rs.getString("paciente_cpf"));
        paciente.setTelefone(rs.getString("paciente_telefone"));
        a.setPaciente(paciente);

        Date data = rs.getDate("data_avaliacao");
        if (data != null) a.setDataAvaliacao(data.toLocalDate());

        a.setProfissionalResponsavel(rs.getString("profissional_responsavel"));
        a.setSexoBiologico(rs.getString("sexo_biologico"));
        a.setIdadeNoMomento(getInteger(rs, "idade_no_momento"));

        a.setPesoKg(getDouble(rs, "peso_kg"));
        a.setAlturaM(getDouble(rs, "altura_m"));
        a.setObjetivoPaciente(rs.getString("objetivo_paciente"));
        a.setNivelAtividadeFisica(rs.getString("nivel_atividade_fisica"));

        a.setDobraTricipitalMm(getDouble(rs, "dobra_tricipital_mm"));
        a.setDobraBicipitalMm(getDouble(rs, "dobra_bicipital_mm"));
        a.setDobraAbdominalMm(getDouble(rs, "dobra_abdominal_mm"));
        a.setDobraSubescapularMm(getDouble(rs, "dobra_subescapular_mm"));
        a.setDobraAxilarMediaMm(getDouble(rs, "dobra_axilar_media_mm"));
        a.setDobraCoxaMm(getDouble(rs, "dobra_coxa_mm"));
        a.setDobraToracicaMm(getDouble(rs, "dobra_toracica_mm"));
        a.setDobraSuprailiacaMm(getDouble(rs, "dobra_suprailíaca_mm"));
        a.setDobraPanturrilhaMm(getDouble(rs, "dobra_panturrilha_mm"));

        a.setCircPescocoCm(getDouble(rs, "circ_pescoco_cm"));
        a.setCircToraxCm(getDouble(rs, "circ_torax_cm"));
        a.setCircOmbroCm(getDouble(rs, "circ_ombro_cm"));
        a.setCircCinturaCm(getDouble(rs, "circ_cintura_cm"));
        a.setCircQuadrilCm(getDouble(rs, "circ_quadril_cm"));
        a.setCircAbdomenCm(getDouble(rs, "circ_abdomen_cm"));

        a.setCircBracoEsqRelaxadoCm(getDouble(rs, "circ_braco_esq_relaxado_cm"));
        a.setCircBracoDirRelaxadoCm(getDouble(rs, "circ_braco_dir_relaxado_cm"));
        a.setCircBracoEsqContraidoCm(getDouble(rs, "circ_braco_esq_contraido_cm"));
        a.setCircBracoDirContraidoCm(getDouble(rs, "circ_braco_dir_contraido_cm"));

        a.setCircAntebracoEsqCm(getDouble(rs, "circ_antebraco_esq_cm"));
        a.setCircAntebracoDirCm(getDouble(rs, "circ_antebraco_dir_cm"));

        a.setCircCoxaEsqProximalCm(getDouble(rs, "circ_coxa_esq_proximal_cm"));
        a.setCircCoxaDirProximalCm(getDouble(rs, "circ_coxa_dir_proximal_cm"));
        a.setCircCoxaEsqMedialCm(getDouble(rs, "circ_coxa_esq_medial_cm"));
        a.setCircCoxaDirMedialCm(getDouble(rs, "circ_coxa_dir_medial_cm"));
        a.setCircCoxaEsqDistalCm(getDouble(rs, "circ_coxa_esq_distal_cm"));
        a.setCircCoxaDirDistalCm(getDouble(rs, "circ_coxa_dir_distal_cm"));

        a.setCircPanturrilhaEsqCm(getDouble(rs, "circ_panturrilha_esq_cm"));
        a.setCircPanturrilhaDirCm(getDouble(rs, "circ_panturrilha_dir_cm"));

        a.setImc(getDouble(rs, "imc"));
        a.setClassificacaoImc(rs.getString("classificacao_imc"));
        a.setInterpretacaoImc(rs.getString("interpretacao_imc"));

        a.setPercentualGordura(getDouble(rs, "percentual_gordura"));
        a.setProtocoloGordura(rs.getString("protocolo_gordura"));
        a.setMassaGordaKg(getDouble(rs, "massa_gorda_kg"));
        a.setMassaMagraKg(getDouble(rs, "massa_magra_kg"));

        a.setRcq(getDouble(rs, "rcq"));
        a.setClassificacaoRcq(rs.getString("classificacao_rcq"));

        a.setObservacoesGerais(rs.getString("observacoes_gerais"));
        a.setObservacoesNutricionais(rs.getString("observacoes_nutricionais"));
        a.setAnotacoesProfissional(rs.getString("anotacoes_profissional"));

        Timestamp criado = rs.getTimestamp("criado_em");
        if (criado != null) a.setCriadoEm(criado.toLocalDateTime());

        Timestamp atualizado = rs.getTimestamp("atualizado_em");
        if (atualizado != null) a.setAtualizadoEm(atualizado.toLocalDateTime());

        return a;
    }

    private void setString(PreparedStatement ps, int index, String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) ps.setNull(index, Types.VARCHAR);
        else ps.setString(index, valor.trim());
    }

    private void setLong(PreparedStatement ps, int index, Long valor) throws SQLException {
        if (valor == null) ps.setNull(index, Types.BIGINT);
        else ps.setLong(index, valor);
    }

    private void setInteger(PreparedStatement ps, int index, Integer valor) throws SQLException {
        if (valor == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, valor);
    }

    private void setDouble(PreparedStatement ps, int index, Double valor) throws SQLException {
        if (valor == null) ps.setNull(index, Types.NUMERIC);
        else ps.setDouble(index, valor);
    }

    private void setDate(PreparedStatement ps, int index, java.time.LocalDate valor) throws SQLException {
        if (valor == null) ps.setNull(index, Types.DATE);
        else ps.setDate(index, Date.valueOf(valor));
    }

    private Double getDouble(ResultSet rs, String coluna) throws SQLException {
        double valor = rs.getDouble(coluna);
        return rs.wasNull() ? null : valor;
    }

    private Integer getInteger(ResultSet rs, String coluna) throws SQLException {
        int valor = rs.getInt(coluna);
        return rs.wasNull() ? null : valor;
    }
}