package br.com.clinica.model;

public class Usuario {

    private Integer id;
    private String nome;
    private String login;
    private String senha;
    private boolean ativo;
    private Perfil perfil;

    public Usuario() {
    }

    public Usuario(Integer id, String nome, String login, String senha, boolean ativo, Perfil perfil) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.ativo = ativo;
        this.perfil = perfil;
    }

    private String pessoaNome;

    public String getPessoaNome() {
        return pessoaNome;
    }

    public void setPessoaNome(String pessoaNome) {
        this.pessoaNome = pessoaNome;
    }


    // === ID ===
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // === NOME ===
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // === LOGIN ===
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    // === SENHA ===
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    // === ATIVO ===
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // === PERFIL ===
    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    @Override
    public String toString() {
        String pessoa = (pessoaNome != null && !pessoaNome.isBlank()) ? pessoaNome : "(sem nome)";
        return pessoa + " - " + nome + " (" + login + ")";
    }

}
