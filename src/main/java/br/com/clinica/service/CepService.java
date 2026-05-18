package br.com.clinica.service;

import br.com.clinica.dto.CepAddress;
import br.com.clinica.exception.BusinessException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class CepService {

    private static final String VIA_CEP_URL = "https://viacep.com.br/ws/%s/json/";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public CepAddress buscarPorCep(String cep) {

        String cepLimpo = limparCep(cep);

        if (cepLimpo.length() != 8) {
            throw new BusinessException("CEP deve conter 8 dígitos.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(VIA_CEP_URL, cepLimpo)))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            if (response.statusCode() != 200) {
                throw new BusinessException("Não foi possível consultar o CEP.");
            }

            String body = response.body();

            if (body.contains("\"erro\": true")) {
                throw new BusinessException("CEP não encontrado.");
            }

            return parse(body);

        } catch (IOException e) {
            throw new BusinessException("Falha de conexão ao consultar CEP.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Consulta de CEP interrompida.");
        }
    }

    private CepAddress parse(String json) {

        CepAddress address = new CepAddress();

        address.setCep(getJsonValue(json, "cep"));
        address.setRua(getJsonValue(json, "logradouro"));
        address.setBairro(getJsonValue(json, "bairro"));
        address.setCidade(getJsonValue(json, "localidade"));
        address.setUf(getJsonValue(json, "uf"));

        return address;
    }

    private String getJsonValue(String json, String key) {

        String pattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(pattern);

        if (keyIndex < 0) {
            return "";
        }

        int start = json.indexOf("\"", keyIndex + pattern.length());

        if (start < 0) {
            return "";
        }

        int end = json.indexOf("\"", start + 1);

        if (end < 0) {
            return "";
        }

        return json.substring(start + 1, end);
    }

    private String limparCep(String cep) {
        return cep == null ? "" : cep.replaceAll("\\D", "");
    }
}