package ru.javaops.cloudjava.menuservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.javaops.cloudjava.menuservice.dto.CreateMenuRequest;
import ru.javaops.cloudjava.menuservice.dto.MenuItemDto;
import ru.javaops.cloudjava.menuservice.dto.SortBy;
import ru.javaops.cloudjava.menuservice.dto.UpdateMenuRequest;
import ru.javaops.cloudjava.menuservice.exception.DataIntegrityViolationException;
import ru.javaops.cloudjava.menuservice.exception.MenuServiceException;
import ru.javaops.cloudjava.menuservice.mapper.MenuItemMapper;
import ru.javaops.cloudjava.menuservice.service.MenuService;
import ru.javaops.cloudjava.menuservice.storage.model.Category;
import ru.javaops.cloudjava.menuservice.storage.model.MenuItem;
import ru.javaops.cloudjava.menuservice.storage.repositories.MenuItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;

    @Override
    public MenuItemDto createMenuItem(CreateMenuRequest dto) {
        String nameMenuInDb = menuItemRepository.getNameMenuByName(dto.getName());
        if (dto.getName().equals(nameMenuInDb)) {
            throw new DataIntegrityViolationException(String.format("Menu by name=%s - шы already exists", dto.getName()), HttpStatus.CONFLICT);
        }
        MenuItem menuItem = menuItemRepository.save(menuItemMapper.toDomain(dto));
        return menuItemMapper.toDto(menuItem);
    }

    @Override
    public void deleteMenuItem(Long id) {
        if (menuItemRepository.existsById(id)) {
            menuItemRepository.deleteById(id);
        }
    }

    @Override
    public MenuItemDto updateMenuItem(Long id, UpdateMenuRequest update) {
        MenuItemDto itemDto = getMenu(id);
        menuItemRepository.updateMenu(id, update);
        return itemDto;
    }

    @Override
    public MenuItemDto getMenu(Long id) {
        return menuItemRepository.findById(id)
                .map(menuItemMapper::toDto)
                .orElseThrow(() -> new MenuServiceException(String.format("Menu item whith id=%d - not found!", id), HttpStatus.NOT_FOUND));
    }

    @Override
    public List<MenuItemDto> getMenusFor(Category category, SortBy sortBy) {
        return menuItemRepository.getMenusFor(category, sortBy).stream()
                .map(menuItemMapper::toDto)
                .toList();
    }
}
