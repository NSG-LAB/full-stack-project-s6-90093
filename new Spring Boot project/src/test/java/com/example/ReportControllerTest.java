package com.example;

import com.example.controller.ReportController;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void generateValuationPdfReturnsPdfAttachment() throws Exception {
        mockMvc.perform(post("/api/reports/valuation-pdf")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"valuationInput\":{\"areaSqft\":1200},\"valuationResult\":{\"currentValue\":1000000}}"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", Objects.requireNonNull(containsString("application/pdf"))))
                .andExpect(header().string("Content-Disposition", Objects.requireNonNull(containsString("valuation-report-"))));
    }
}

