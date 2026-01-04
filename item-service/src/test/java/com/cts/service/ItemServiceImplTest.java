package com.cts.service;

import com.cts.client.InventoryServiceClient;
import com.cts.dtos.InventoryDto;
import com.cts.dtos.ItemDto;
import com.cts.dtos.ItemInputDto;
import com.cts.entities.Item;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private InventoryServiceClient inventoryServiceClient;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item1;
    private Item item2;
    private ItemDto itemDto1;
    private ItemDto itemDto2;
    private ItemInputDto itemInputDto;

    @BeforeEach
    void init() {
        // Initialize common test objects
        item1 = new Item(1L, "Wireless Mouse", "Ergonomic wireless mouse", "Electronics", BigDecimal.valueOf(25.00), LocalDateTime.now());
        itemDto1 = new ItemDto(1L, "Wireless Mouse", "Ergonomic wireless mouse", "Electronics", BigDecimal.valueOf(25.00), item1.getCreatedAt());

        item2 = new Item(2L, "Keyboard", "Mechanical Keyboard", "Electronics", BigDecimal.valueOf(50.00), LocalDateTime.now());
        itemDto2 = new ItemDto(2L, "Keyboard", "Mechanical Keyboard", "Electronics", BigDecimal.valueOf(50.00), item2.getCreatedAt());

        // Initialize Input DTO for create operations
        itemInputDto = new ItemInputDto(1L, "Wireless Mouse", "Ergonomic wireless mouse", "Electronics", BigDecimal.valueOf(25.00), 10, "Warehouse A", null);
    }

    @Test
    void getAllItems() {
        when(itemRepository.findAll()).thenReturn(List.of(item1, item2));
        when(modelMapper.map(item1, ItemDto.class)).thenReturn(itemDto1);
        when(modelMapper.map(item2, ItemDto.class)).thenReturn(itemDto2);

        var result = itemService.getAllItems();

        assertEquals(2, result.size());
        assertEquals("Wireless Mouse", result.get(0).getName());
        verify(itemRepository).findAll();
    }

    @Test
    void getItemById() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(modelMapper.map(any(Item.class), eq(ItemDto.class))).thenReturn(itemDto1);

        var result = itemService.getItemById(1L);

        assertNotNull(result);
        assertEquals("Wireless Mouse", result.getName());
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_NotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.getItemById(99L));
        verify(itemRepository).findById(99L);
    }

    @Test
    void findItemsByCategory() {
        when(itemRepository.findByCategoryIgnoreCase("Electronics")).thenReturn(List.of(item1, item2));
        when(modelMapper.map(item1, ItemDto.class)).thenReturn(itemDto1);
        when(modelMapper.map(item2, ItemDto.class)).thenReturn(itemDto2);

        var result = itemService.findItemsByCategory("Electronics");

        assertEquals(2, result.size());
        assertEquals("Wireless Mouse", result.get(0).getName());
        verify(itemRepository).findByCategoryIgnoreCase("Electronics");
    }

    @Test
    void createItem() {
        // Mocking mapping from InputDto to Entity
        when(modelMapper.map(any(ItemInputDto.class), eq(Item.class))).thenReturn(item1);
        // Mocking save
        when(itemRepository.save(any(Item.class))).thenReturn(item1);
        // Mocking mapping from Entity to Output Dto
        when(modelMapper.map(any(Item.class), eq(ItemDto.class))).thenReturn(itemDto1);

        // Mock external services
        // FIXED: addInventoryInternal returns InventoryDto, so we must use when().thenReturn()
        when(inventoryServiceClient.addInventoryInternal(any(InventoryDto.class))).thenReturn(new InventoryDto());

        // logEvent is void, so doNothing() is correct
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());

        ItemDto createdItem = itemService.createItem(itemInputDto);

        assertNotNull(createdItem);
        assertEquals("Wireless Mouse", createdItem.getName());

        verify(itemRepository).save(any(Item.class));
        verify(inventoryServiceClient).addInventoryInternal(any(InventoryDto.class));
        verify(auditService).logEvent(eq("CREATE"), eq(1L), anyString());
    }

    @Test
    void updateItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.save(any(Item.class))).thenReturn(item1);
        when(modelMapper.map(any(Item.class), eq(ItemDto.class))).thenReturn(itemDto1);
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());

        ItemDto updatedItem = itemService.updateItem(1L, itemDto1);

        assertNotNull(updatedItem);
        assertEquals("Wireless Mouse", updatedItem.getName());
        verify(itemRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
        verify(auditService).logEvent(eq("UPDATE"), eq(1L), anyString());
    }

    @Test
    void updateItem_NotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.updateItem(99L, itemDto1));
        verify(itemRepository).findById(99L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        doNothing().when(itemRepository).delete(any(Item.class));
        doNothing().when(auditService).logEvent(anyString(), anyLong(), anyString());
        // deleteInventoryByItemId is void, so doNothing() is correct here
        doNothing().when(inventoryServiceClient).deleteInventoryByItemId(anyLong());

        var result = itemService.deleteItem(1L);

        assertNotNull(result);
        assertEquals("Item with ID 1 deleted successfully.", result);
        verify(itemRepository).findById(1L);
        verify(itemRepository).delete(item1);
        verify(inventoryServiceClient).deleteInventoryByItemId(1L);
        verify(auditService).logEvent(eq("DELETE"), eq(1L), anyString());
    }

    @Test
    void deleteItem_NotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemService.deleteItem(99L));
        verify(itemRepository).findById(99L);
        verify(itemRepository, never()).delete(any(Item.class));
    }
}