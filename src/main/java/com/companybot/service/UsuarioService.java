package com.companybot.service;

import com.companybot.dto.UsuarioPerfil;
import com.companybot.entity.Person;
import com.companybot.entity.Usuario;
import com.companybot.repository.PersonRepository;
import com.companybot.repository.UsuarioRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UsuarioService {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UsuarioRepository usuarioRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PersonRepository personRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean validarEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public boolean validarSenha(String senha) {
        if (senha == null) {
            return false;
        }
        int tamanho = senha.trim().replaceAll("\\s+", "").length();
        return tamanho >= 6 && tamanho <= 20;
    }

    /** Equivalente a User::login do PHP (valida hash bcrypt armazenado). */
    @Transactional(readOnly = true)
    public Optional<Usuario> autenticar(String deslogin, String despassword) {
        Optional<Usuario> usuario = usuarioRepository.findLatestByDeslogin(deslogin);
        if (usuario.isEmpty() || !passwordEncoder.matches(despassword, usuario.get().getDespassword())) {
            return Optional.empty();
        }
        return usuario;
    }

    /** Equivalente a User::getFromSession (usuario logado + dados da tb_persons). */
    @Transactional(readOnly = true)
    public Optional<UsuarioPerfil> perfilAtual() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return byDeslogin(auth.getName());
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioPerfil> byDeslogin(String deslogin) {
        Optional<Usuario> usuario = usuarioRepository.findLatestByDeslogin(deslogin);
        if (usuario.isEmpty()) {
            return Optional.empty();
        }
        Optional<Person> person = personRepository.findById(usuario.get().getIdperson());
        return Optional.of(UsuarioPerfil.of(usuario.get(), person.orElse(null)));
    }

    /** Registro novo (POST /registrar). */
    @Transactional
    public Usuario registrar(String nome, String sobrenome, String deslogin, String despassword)
        throws IllegalArgumentException {

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Por favor, informar o nome!");
        }
        if (!validarEmail(deslogin)) {
            throw new IllegalArgumentException("Por favor, informar e-mail em um formato válido!");
        }
        if (!validarSenha(despassword)) {
            throw new IllegalArgumentException("A senha deve ter, no minimo 6 caracteres, e no máximo 20!");
        }
        if (usuarioRepository.existsByDeslogin(deslogin)) {
            throw new IllegalArgumentException("Este e-mail já está cadastrado!");
        }

        String desnick = (nome + " " + (sobrenome == null ? "" : sobrenome)).trim();

        Person person = new Person();
        person.setDesperson(nome);
        person.setDesemail(deslogin);
        person.setDesnick(desnick);
        person.setDtregister(LocalDateTime.now());
        person = personRepository.save(person);

        Usuario usuario = new Usuario();
        usuario.setIdperson(person.getIdperson());
        usuario.setDeslogin(deslogin.trim());
        usuario.setDespassword(passwordEncoder.encode(despassword));
        usuario.setInadmin(0);
        usuario.setDtregister(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    /** Busca a tb_persons vinculada ao login (usado no fluxo "esqueci minha senha"). */
    @Transactional(readOnly = true)
    public Optional<Person> pessoaPorDeslogin(String email) {
        Optional<Usuario> usuario = usuarioRepository.findLatestByDeslogin(email);
        if (usuario.isEmpty()) {
            return Optional.empty();
        }
        return personRepository.findById(usuario.get().getIdperson());
    }

    /** Atualiza dados do perfil do usuário logado (campos vazios preservam o valor atual). */
    @Transactional
    public void atualizarPerfil(String deslogin, String desperson, Integer nrddd, Long nrphone, LocalDate dtbirth)
        throws IllegalArgumentException {

        Optional<Usuario> usuarioOpt = usuarioRepository.findLatestByDeslogin(deslogin);
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado!");
        }
        Optional<Person> personOpt = personRepository.findById(usuarioOpt.get().getIdperson());
        if (personOpt.isEmpty()) {
            throw new IllegalArgumentException("Perfil não encontrado!");
        }
        Person person = personOpt.get();

        if (desperson != null && !desperson.isBlank()) {
            person.setDesperson(desperson.trim());
        }
        if (nrddd != null && (nrddd < 1 || nrddd > 99)) {
            throw new IllegalArgumentException("DDD deve ser um número entre 1 e 99!");
        }
        if (nrddd != null) {
            person.setNrddd(nrddd);
        }
        if (nrphone != null) {
            int digitos = String.valueOf(nrphone).length();
            if (digitos < 8 || digitos > 9) {
                throw new IllegalArgumentException("Telefone deve ter 8 ou 9 dígitos!");
            }
            person.setNrphone(nrphone);
        }
        if (dtbirth != null && dtbirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento não pode ser no futuro!");
        }
        if (dtbirth != null) {
            person.setDtbirth(dtbirth);
        }
        personRepository.save(person);
    }
}