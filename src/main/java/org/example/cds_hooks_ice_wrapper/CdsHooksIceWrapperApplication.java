package org.example.cds_hooks_ice_wrapper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class CdsHooksIceWrapperApplication {

	public static void main(String[] args) {
		SpringApplication.run(CdsHooksIceWrapperApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}