package br.com.clinica.model;

public class Usuario {

    private Integer id;
    private String nome;        // no seu projeto, isso é o CARGO (ex: ENFERMEIRA)
    private String pessoaNome;  // nome da pessoa
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // CARGO
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    // NOME DA PESSOA
    public String getPessoaNome() {
        return pessoaNome;
    }

    public void setPessoaNome(String pessoaNome) {
        this.pessoaNome = pessoaNome;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    @Override
    public String toString() {
        String pessoa = (pessoaNome != null && !pessoaNome.isBlank()) ? pessoaNome : "";
        String cargo = (nome != null && !nome.isBlank()) ? nome : "";

        if (cargo.isBlank() && perfil != null && perfil.getNome() != null) {
            cargo = perfil.getNome();
        }

        if (pessoa.isBlank() && login != null && !login.isBlank()) {
            pessoa = login; // fallback
        }

        if (cargo.isBlank()) return pessoa;
        return pessoa + " (" + cargo + ")";
    }
}