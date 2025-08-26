package ru.javaops.cloudjava.menuservice.storage.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.javaops.cloudjava.menuservice.dto.SortBy;
import ru.javaops.cloudjava.menuservice.dto.UpdateMenuRequest;
import ru.javaops.cloudjava.menuservice.storage.model.Category;
import ru.javaops.cloudjava.menuservice.storage.model.MenuItem;
import ru.javaops.cloudjava.menuservice.storage.model.MenuItem_;
import ru.javaops.cloudjava.menuservice.storage.repositories.updaters.MenuAttrUpdater;

import java.util.List;
import java.util.Optional;

@Repository
public class CustomMenuItemRepositoryImpl implements CustomMenuItemRepository {

    private final EntityManager em;
    private final List<MenuAttrUpdater<?>> updaters;

    public CustomMenuItemRepositoryImpl(EntityManager em, List<MenuAttrUpdater<?>> updaters) {
        this.em = em;
        this.updaters = updaters;
    }

    @Transactional
    @Override
    public int updateMenu(Long id, UpdateMenuRequest dto) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<MenuItem> criteriaUpdate = cb.createCriteriaUpdate(MenuItem.class);
        Root<MenuItem> root = criteriaUpdate.from(MenuItem.class);

        Optional<MenuItem> existingItem = Optional.ofNullable(em.find(MenuItem.class, id));
        if (existingItem.isEmpty()) {
            throw new EntityNotFoundException("Меню с ID " + id + " не найдено");
        }

        updaters.forEach(updater -> updater.updateAttr(criteriaUpdate, dto));
        criteriaUpdate.where(cb.equal(root.get(MenuItem_.id), id));

        return em.createQuery(criteriaUpdate).executeUpdate();
    }

    @Override
    public List<MenuItem> getMenusFor(Category category, SortBy sortBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MenuItem> criteriaQuery = cb.createQuery(MenuItem.class);
        Root<MenuItem> root = criteriaQuery.from(MenuItem.class);

        // Добавляем условие категории
        Predicate categoryPredicate = cb.equal(root.get(MenuItem_.category), category);
        criteriaQuery.where(categoryPredicate);

        // Устанавливаем сортировку
        switch (sortBy) {
            case PRICE_ASC:
                criteriaQuery.orderBy(SortBy.PRICE_ASC.getOrder(cb, root));
                break;
            case PRICE_DESC:
                criteriaQuery.orderBy(SortBy.PRICE_DESC.getOrder(cb, root));
                break;
            case AZ:
                criteriaQuery.orderBy(SortBy.AZ.getOrder(cb, root));
                break;
            case ZA:
                criteriaQuery.orderBy(SortBy.ZA.getOrder(cb, root));
                break;
            case DATE_ASC:
                criteriaQuery.orderBy(SortBy.DATE_ASC.getOrder(cb, root));
                break;
            case DATE_DESC:
                criteriaQuery.orderBy(SortBy.DATE_DESC.getOrder(cb, root));
                break;
        }

        return em.createQuery(criteriaQuery)
                .getResultList();
    }
}
