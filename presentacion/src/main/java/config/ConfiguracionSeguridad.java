package monedas.api.presentacion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

import monedas.api.aplicacion.seguridad.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class ConfiguracionSeguridad {

    @Autowired
    private FiltroSeguridad filtro;

    @Bean
    public UserDetailsService servicioUsuario() {
        return new UsuarioDetalleServicio();
    }

    // Configurando Seguridad Http
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // Deshabilita la protección CSRF
                .cors(Customizer.withDefaults()) // Activa CORS
                .authorizeHttpRequests(
                        (authz) -> authz
                                .antMatchers("/api/usuarios/validar/**").permitAll()
                                .antMatchers(
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui.html",
                                        "/swagger-resources/**",
                                        "/webjars/**")
                                .permitAll()
                                // .antMatchers("/api/monedas/**").permitAll()
                                .anyRequest().authenticated())
                .addFilterAfter(filtro, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

}
