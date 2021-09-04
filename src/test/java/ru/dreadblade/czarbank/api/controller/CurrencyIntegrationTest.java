package ru.dreadblade.czarbank.api.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import ru.dreadblade.czarbank.api.mapper.CurrencyMapper;
import ru.dreadblade.czarbank.api.model.response.CurrencyResponseDTO;
import ru.dreadblade.czarbank.repository.CurrencyRepository;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@DisplayName("Currency Integration Tests")
@Sql(value = { "/user/users-insertion.sql", "/bank-account/bank-accounts-insertion.sql" }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = { "/bank-account/bank-accounts-deletion.sql", "/user/users-deletion.sql" }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CurrencyIntegrationTest extends BaseIntegrationTest {
    public static final String CURRENCIES_API_URL = "/api/currencies";

    @Autowired
    CurrencyRepository currencyRepository;

    @Autowired
    CurrencyMapper currencyMapper;

    @Nested
    @DisplayName("findAll() Tests")
    class findAllTests {
        @Test
        void findAll_isSuccessful() throws Exception {
            long expectedSize = currencyRepository.count();

            List<CurrencyResponseDTO> expectedDTOs = currencyRepository.findAll().stream()
                    .map(currencyMapper::entityToResponseDTO)
                    .collect(Collectors.toList());

            String expectedResponse = objectMapper.writeValueAsString(expectedDTOs);

            mockMvc.perform(get(CURRENCIES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))))
                    .andExpect(content().json(expectedResponse));
        }

        @Test
        @Rollback
        void findAll_isEmpty() throws Exception {
            currencyRepository.deleteAll();

            long expectedSize = 0L;

            Assertions.assertThat(currencyRepository.count()).isEqualTo(expectedSize);

            mockMvc.perform(get(CURRENCIES_API_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(Math.toIntExact(expectedSize))));
        }
    }
}
