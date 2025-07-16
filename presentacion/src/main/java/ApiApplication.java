package monedas.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "monedas.api.presentacion",
    "monedas.api.aplicacion",
    "monedas.api.infraestructura",
    "monedas.api.core"
})
@EntityScan("monedas.api.dominio.entidades")
@EnableJpaRepositories("monedas.api.infraestructura.repositorios")
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
