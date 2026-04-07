package com.example;

import com.example.controller.RoiController;
import com.example.model.Recommendation;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import com.example.service.RoiPlannerService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoiController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationRepository recommendationRepository;

    @MockBean
    private RoiPlannerService roiPlannerService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void createPlanReturnsPlan() throws Exception {
        Recommendation recommendation = new Recommendation();
        recommendation.setId(1L);
        recommendation.setTitle("Kitchen Upgrade");
        recommendation.setCategory("kitchen-bathroom");
        recommendation.setDifficulty("easy");
        recommendation.setExpectedRoi(BigDecimal.valueOf(25));
        recommendation.setRoiPercentage(25.0);
        recommendation.setEstimatedCostJson("{\"min\":1000,\"max\":3000}");
        recommendation.setApplicablePropertyTypesJson("[]");
        recommendation.setApplicableConditionsJson("[]");
        recommendation.setApplicableCitiesJson("[]");

        Mockito.when(recommendationRepository.findByIsActiveTrue(ArgumentMatchers.any())).thenReturn(List.of(recommendation));
        Mockito.when(roiPlannerService.createPlan(ArgumentMatchers.anyList(), ArgumentMatchers.anyDouble(), ArgumentMatchers.anyString(), ArgumentMatchers.anyInt()))
                .thenReturn(Map.of("selectedCount", 1, "budget", 5000));

        mockMvc.perform(post("/api/roi/plan")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"budget\":5000,\"propertyType\":\"house\",\"propertyCondition\":\"good\",\"city\":\"Sydney\",\"topN\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ROI plan generated"))
                .andExpect(jsonPath("$.plan.selectedCount").value(1));
    }

    @Test
    public void createPlanReturnsValidationError() throws Exception {
        mockMvc.perform(post("/api/roi/plan")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"topN\":99,\"propertyCondition\":\"bad\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errors").isArray());
    }
}

