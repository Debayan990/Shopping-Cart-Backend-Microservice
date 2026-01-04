package com.cts.controllers;

import com.cts.dtos.ItemDto;
import com.cts.dtos.ItemInputDto;
import com.cts.dtos.SuccessDto;
import com.cts.security.JwtTokenProvider; // Import the TokenProvider
import com.cts.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    // FIX: Mock the JwtTokenProvider to bypass real JWT validation
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private ItemInputDto itemInputDto;

    @BeforeEach
    void init() {
        // Setup Test Data
        itemDto1 = new ItemDto(1L, "Wireless Mouse", "A great mouse", "Electronics", BigDecimal.valueOf(25.0), LocalDateTime.now());
        itemDto2 = new ItemDto(2L, "Keyboard", "A great keyboard", "Electronics", BigDecimal.valueOf(50.0), LocalDateTime.now());

        // Input DTO for create test
        itemInputDto = new ItemInputDto(null, "Wireless Mouse", "A great mouse", "Electronics", BigDecimal.valueOf(25.0), 10, "Warehouse A", null);

        // FIX: Stub the JWT Provider to accept our dummy "token"
        when(jwtTokenProvider.validateToken("token")).thenReturn(true);
        when(jwtTokenProvider.getUsername("token")).thenReturn("admin");
        // Ensure the user has ROLE_ADMIN to pass @PreAuthorize("hasRole('ADMIN')")
        when(jwtTokenProvider.getRoles("token")).thenReturn(List.of("ROLE_ADMIN"));
    }

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(any(ItemInputDto.class))).thenReturn(itemDto1);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemInputDto)) // Use InputDto for request body
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    void getAllItems() throws Exception {
        when(itemService.getAllItems()).thenReturn(List.of(itemDto1, itemDto2));

        mockMvc.perform(get("/api/items")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].name").value("Wireless Mouse"));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItemById(1L)).thenReturn(itemDto1);

        mockMvc.perform(get("/api/items/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Wireless Mouse"));
    }

    @Test
    void updateItem() throws Exception {
        ItemDto updatedDto = new ItemDto(1L, "Updated Mouse", "Updated Desc", "Peripherals", BigDecimal.valueOf(30.0), LocalDateTime.now());
        when(itemService.updateItem(eq(1L), any(ItemDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/api/items/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Mouse"));
    }

    @Test
    void searchByCategory() throws Exception {
        when(itemService.findItemsByCategory("Electronics")).thenReturn(List.of(itemDto1, itemDto2));

        mockMvc.perform(get("/api/items/search/category")
                        .param("category", "Electronics")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void deleteItem() throws Exception {
        SuccessDto successDto = new SuccessDto("Item with ID 1 deleted successfully.");
        when(itemService.deleteItem(1L)).thenReturn(successDto.getMessage());

        mockMvc.perform(delete("/api/items/{id}", 1L)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item with ID 1 deleted successfully."));
    }
}