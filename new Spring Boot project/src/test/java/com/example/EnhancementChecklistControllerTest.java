package com.example;

import com.example.controller.EnhancementChecklistController;
import com.example.model.EnhancementChecklist;
import com.example.repository.EnhancementChecklistRepository;
import com.example.repository.UserRepository;
import com.example.security.JwtUtil;
import com.example.service.EnhancementChecklistService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EnhancementChecklistController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EnhancementChecklistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnhancementChecklistService enhancementChecklistService;

    @MockBean
    private EnhancementChecklistRepository enhancementChecklistRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserRepository userRepository;

    @Test
    public void createChecklistItemReturnsCreated() throws Exception {
        EnhancementChecklist checklist = checklist(1L);
        Mockito.when(enhancementChecklistService.createChecklistItem(ArgumentMatchers.anyMap())).thenReturn(checklist);
        Mockito.when(enhancementChecklistService.parseAttachmentUrls(ArgumentMatchers.anyString())).thenReturn(List.of());

        mockMvc.perform(post("/api/enhancement-checklist")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                        .content("{\"propertyId\":10,\"userId\":1,\"type\":\"before\",\"item\":\"Paint wall\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.type").value("before"));
    }

    @Test
    public void getChecklistItemsReturnsList() throws Exception {
        EnhancementChecklist checklist = checklist(2L);
        Mockito.when(enhancementChecklistService.getChecklistItems(10L, "before")).thenReturn(List.of(checklist));
        Mockito.when(enhancementChecklistService.parseAttachmentUrls(ArgumentMatchers.anyString())).thenReturn(List.of());

        mockMvc.perform(get("/api/enhancement-checklist/10/before"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].item").value("Repair tile"));
    }

    @Test
    @SuppressWarnings("null")
    public void uploadPhotosToChecklistReturnsUrls() throws Exception {
        EnhancementChecklist checklist = checklist(3L);
        checklist.setAttachmentUrlsJson("[]");

        Mockito.when(enhancementChecklistRepository.findById(3L)).thenReturn(Optional.of(checklist));
        Mockito.when(enhancementChecklistService.parseAttachmentUrls("[]")).thenReturn(List.of());
        Mockito.when(enhancementChecklistService.toJson(ArgumentMatchers.any())).thenReturn("[\"/uploads/checklist/sample.jpg\"]");
        Mockito.doReturn(checklist).when(enhancementChecklistRepository).save(ArgumentMatchers.any(EnhancementChecklist.class));

        MockMultipartFile file = new MockMultipartFile("photos", "sample.jpg", "image/jpeg", "abc".getBytes());

        mockMvc.perform(multipart("/api/enhancement-checklist/upload/3").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.urls[0]", Objects.requireNonNull(containsString("/uploads/checklist/"))));
    }

    @Test
    public void deleteChecklistItemReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/enhancement-checklist/4"))
                .andExpect(status().isNoContent());

        Mockito.verify(enhancementChecklistService).deleteChecklistItem(4L);
    }

    private EnhancementChecklist checklist(Long id) {
        EnhancementChecklist checklist = new EnhancementChecklist();
        checklist.setId(id);
        checklist.setPropertyId(10L);
        checklist.setUserId(1L);
        checklist.setType("before");
        checklist.setItem("Repair tile");
        checklist.setCompleted(false);
        checklist.setNotes("note");
        checklist.setAttachmentUrlsJson("[]");
        return checklist;
    }
}
