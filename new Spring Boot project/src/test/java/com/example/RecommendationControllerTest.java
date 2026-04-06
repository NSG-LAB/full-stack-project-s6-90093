package com.example;

import com.example.controller.RecommendationController;
import com.example.model.Property;
import com.example.model.Recommendation;
import com.example.repository.PropertyRepository;
import com.example.repository.RecommendationRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtPrincipal;
import com.example.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RecommendationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationRepository recommendationRepository;

    @MockBean
    private PropertyRepository propertyRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void getRecommendationsReturnsFilteredList() throws Exception {
        Recommendation rec1 = recommendation(1L, "Kitchen Renovation", "kitchen-bathroom", "easy", 90);
        rec1.setRelatedRecommendationIdsJson("[2]");
        Recommendation rec2 = recommendation(2L, "Paint Refresh", "wall-paint", "easy", 30);

        Mockito.when(recommendationRepository.findByIsActiveTrue(ArgumentMatchers.any()))
                .thenReturn(List.of(rec1, rec2));
        Mockito.when(recommendationRepository.findAllById(ArgumentMatchers.anyIterable()))
                .thenReturn(List.of(rec2));

        mockMvc.perform(get("/api/recommendations")
                        .param("q", "kitchen")
                        .param("limit", "10")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.recommendations[0].id").value(1))
                .andExpect(jsonPath("$.recommendations[0].relatedRecommendations[0].id").value(2));
    }

    @Test
    public void getRecommendationsForPropertyReturnsMatches() throws Exception {
        Property property = new Property();
        property.setId(10L);
        property.setPropertyType("house");
        property.setCondition("good");
        property.setLocationCity("Sydney");

        Recommendation matching = recommendation(3L, "Solar Upgrade", "energy-efficiency", "moderate", 80);
        matching.setApplicablePropertyTypesJson("[\"all\"]");
        matching.setApplicableConditionsJson("[]");
        matching.setApplicableCitiesJson("[]");

        Recommendation nonMatching = recommendation(4L, "Apartment Kitchen", "kitchen-bathroom", "moderate", 70);
        nonMatching.setApplicablePropertyTypesJson("[\"apartment\"]");
        nonMatching.setApplicableConditionsJson("[]");
        nonMatching.setApplicableCitiesJson("[]");

        Mockito.when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        Mockito.when(recommendationRepository.findByIsActiveTrue(ArgumentMatchers.any()))
                .thenReturn(List.of(matching, nonMatching));

        mockMvc.perform(get("/api/recommendations/property/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.recommendations[0].id").value(3));
    }

    @Test
    public void createRecommendationRejectsNonAdmin() throws Exception {
        mockMvc.perform(post("/api/recommendations")
                        .with(jwtPrincipal("user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"A\",\"category\":\"flooring\",\"description\":\"D\",\"expectedROI\":10,\"difficulty\":\"easy\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Admin access required"));
    }

    @Test
    public void createRecommendationReturnsCreatedForAdmin() throws Exception {
        Recommendation related = recommendation(2L, "Related", "flooring", "easy", 20);

        Recommendation saved = recommendation(5L, "Roof Repair", "safety-security", "moderate", 75);
        saved.setRelatedRecommendationIdsJson("[2]");

        Mockito.when(recommendationRepository.save(ArgumentMatchers.any(Recommendation.class))).thenReturn(saved);
        Mockito.when(recommendationRepository.findAllById(ArgumentMatchers.anyIterable())).thenReturn(List.of(related));

        mockMvc.perform(post("/api/recommendations")
                        .with(jwtPrincipal("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Roof Repair\",\"category\":\"safety-security\",\"description\":\"Improve roof\",\"expectedROI\":75,\"difficulty\":\"moderate\",\"relatedRecommendationIds\":[2]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Recommendation created successfully"))
                .andExpect(jsonPath("$.recommendation.id").value(5))
                .andExpect(jsonPath("$.recommendation.relatedRecommendations[0].id").value(2));
    }

    private RequestPostProcessor jwtPrincipal(String role) {
        JwtPrincipal principal = new JwtPrincipal(1L, "user@demo.com", role);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
        );

        return request -> {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authenticationToken);
            SecurityContextHolder.setContext(context);
            return request;
        };
    }

    private Recommendation recommendation(Long id, String title, String category, String difficulty, int expectedRoi) {
        Recommendation recommendation = new Recommendation();
        recommendation.setId(id);
        recommendation.setTitle(title);
        recommendation.setCategory(category);
        recommendation.setDescription("Description");
        recommendation.setBenefitsJson("[]");
        recommendation.setEstimatedCostJson("{\"min\":1000,\"max\":3000}");
        recommendation.setExpectedRoi(BigDecimal.valueOf(expectedRoi));
        recommendation.setRoiPercentage(15.0);
        recommendation.setDifficulty(difficulty);
        recommendation.setImagesJson("[]");
        recommendation.setTipsJson("[]");
        recommendation.setApplicablePropertyTypesJson("[]");
        recommendation.setApplicableCitiesJson("[]");
        recommendation.setApplicableConditionsJson("[]");
        recommendation.setBeforeAfterImagesJson("[]");
        recommendation.setRelatedRecommendationIdsJson("[]");
        recommendation.setPriority(5);
        recommendation.setIsActive(true);
        return recommendation;
    }
}
