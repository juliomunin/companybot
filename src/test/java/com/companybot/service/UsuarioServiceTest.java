package com.companybot.service;

import com.companybot.entity.Person;
import com.companybot.entity.Usuario;
import com.companybot.repository.PersonRepository;
import com.companybot.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuarioServiceTest {

    private UsuarioRepository usuarioRepository;
    private PersonRepository personRepository;
    private PasswordEncoder passwordEncoder;
    private UsuarioService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        personRepository = mock(PersonRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        service = new UsuarioService(usuarioRepository, personRepository, passwordEncoder);
    }

    @Test
    void validaEmailESenha() {
        assertThat(service.validarEmail("joao@example.com")).isTrue();
        assertThat(service.validarEmail(" joao@example.com ")).isTrue();
        assertThat(service.validarEmail("joao@example")).isFalse();
        assertThat(service.validarEmail(" ")).isFalse();
        assertThat(service.validarEmail(null)).isFalse();

        assertThat(service.validarSenha("123456")).isTrue();
        assertThat(service.validarSenha("12345")).isFalse();
        assertThat(service.validarSenha("123456789012345678901")).isFalse();
        assertThat(service.validarSenha(null)).isFalse();
    }

    @Test
    void registrarNormalizaLoginESalvaNomeCompleto() {
        when(usuarioRepository.existsByDesloginIgnoreCase("joao@example.com")).thenReturn(false);
        when(passwordEncoder.encode("segredo1")).thenReturn("hash");
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
            Person person = invocation.getArgument(0);
            person.setIdperson(10L);
            return person;
        });
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = service.registrar("  João   ", "  da Silva ", " JOAO@Example.com ", "segredo1");

        assertThat(usuario.getDeslogin()).isEqualTo("joao@example.com");
        assertThat(usuario.getDespassword()).isEqualTo("hash");
        assertThat(usuario.getInadmin()).isZero();
        assertThat(usuario.getIdperson()).isEqualTo(10L);

        ArgumentCaptor<Person> personCaptor = ArgumentCaptor.forClass(Person.class);
        verify(personRepository).save(personCaptor.capture());
        assertThat(personCaptor.getValue().getDesperson()).isEqualTo("João da Silva");
        assertThat(personCaptor.getValue().getDesnick()).isEqualTo("João da Silva");
        assertThat(personCaptor.getValue().getDesemail()).isEqualTo("joao@example.com");
    }

    @Test
    void registrarRejeitaEmailDuplicadoSemSalvar() {
        when(usuarioRepository.existsByDesloginIgnoreCase("joao@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar("João", null, "joao@example.com", "segredo1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("já está cadastrado");

        verify(personRepository, never()).save(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void registrarRejeitaDadosInvalidos() {
        assertThatThrownBy(() -> service.registrar(" ", null, "joao@example.com", "segredo1"))
                .hasMessageContaining("informe o nome");

        assertThatThrownBy(() -> service.registrar("João", null, "email-invalido", "segredo1"))
                .hasMessageContaining("e-mail em formato válido");

        assertThatThrownBy(() -> service.registrar("João", null, "joao@example.com", "123"))
                .hasMessageContaining("mínimo, 6");
    }

    @Test
    void atualizarPerfilPreservaNomeVazioEValidaTelefone() {
        Usuario usuario = new Usuario();
        usuario.setIdperson(1L);
        when(usuarioRepository.findLatestByDeslogin(anyString())).thenReturn(Optional.of(usuario));

        Person person = new Person();
        person.setDesperson("João");
        when(personRepository.findById(1L)).thenReturn(Optional.of(person));

        service.atualizarPerfil("joao@example.com", "  ", 11, 988887777L, LocalDate.of(1990, 5, 20));

        assertThat(person.getDesperson()).isEqualTo("João");
        assertThat(person.getNrddd()).isEqualTo(11);
        assertThat(person.getNrphone()).isEqualTo(988887777L);
        assertThat(person.getDtbirth()).isEqualTo(LocalDate.of(1990, 5, 20));
        verify(personRepository).save(person);
    }

    @Test
    void atualizarPerfilRejeitaDddTelefoneEDataNoFuturo() {
        Usuario usuario = new Usuario();
        usuario.setIdperson(1L);
        when(usuarioRepository.findLatestByDeslogin(anyString())).thenReturn(Optional.of(usuario));
        when(personRepository.findById(1L)).thenReturn(Optional.of(new Person()));

        assertThatThrownBy(() -> service.atualizarPerfil("joao@example.com", "João", 0, null, null))
                .hasMessageContaining("DDD");

        assertThatThrownBy(() -> service.atualizarPerfil("joao@example.com", "João", 11, 1234567L, null))
                .hasMessageContaining("8 ou 9 dígitos");

        assertThatThrownBy(() -> service.atualizarPerfil("joao@example.com", "João", 11, null,
                LocalDate.now().plusDays(1)))
                .hasMessageContaining("futuro");
    }

    @Test
    void autenticarRejeitaSenhaIncorreta() {
        Usuario usuario = new Usuario();
        usuario.setDespassword("hash");
        when(usuarioRepository.findLatestByDeslogin("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThat(service.autenticar("joao@example.com", "errada")).isEmpty();
    }

    @Test
    void alterarSenhaAtualizaHashDoUsuario() {
        Usuario usuario = new Usuario();
        when(usuarioRepository.findLatestByDeslogin("joao@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("novaSenha1")).thenReturn("novo-hash");

        service.alterarSenha("joao@example.com", "novaSenha1");

        assertThat(usuario.getDespassword()).isEqualTo("novo-hash");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void alterarSenhaRejeitaSenhaInvalidaEUsuarioInexistente() {
        assertThatThrownBy(() -> service.alterarSenha("joao@example.com", "123"))
                .hasMessageContaining("mínimo, 6");

        when(usuarioRepository.findLatestByDeslogin("joao@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alterarSenha("joao@example.com", "senhaValida1"))
                .hasMessageContaining("não encontrado");
    }
}
