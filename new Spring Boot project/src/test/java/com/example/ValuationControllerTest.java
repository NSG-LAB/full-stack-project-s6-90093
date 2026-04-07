package com.example;

import com.example.controller.ValuationController;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import com.example.service.ValuationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ValuationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ValuationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValuationService valuationService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void estimateReturnsResultForValidPayload() throws Exception {
        Mockito.when(valuationService.estimateValue(Mockito.anyMap())).thenReturn(Map.of(
                "currentValue", 1000000,
                "improvedValue", 1080000,
                "confidence", "medium"
        ));

        mockMvc.perform(post("/api/valuations/estimate")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content("{\"areaSqft\":1200,\"ageYears\":5,\"bedrooms\":3,\"bathrooms\":2,\"conditionScore\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentValue").value(1000000))
                .andExpect(jsonPath("$.improvedValue").value(1080000))
                .andExpect(jsonPath("$.confidence").value("medium"));
    }

    @Test
    public void estimateReturnsValidationErrorsForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/valuations/estimate")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content("{\"areaSqft\":90,\"ageYears\":-1,\"bedrooms\":-1,\"bathrooms\":-1,\"conditionScore\":8}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}

