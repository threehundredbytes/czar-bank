package ru.dreadblade.czarbank;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Disabled
class CzarBankApplicationTests {
	@Autowired
	WebApplicationContext webApplicationContext;

	@Test
	void contextLoads() {
//		assertThat(webApplicationContext).isNotNull();
	}
}
