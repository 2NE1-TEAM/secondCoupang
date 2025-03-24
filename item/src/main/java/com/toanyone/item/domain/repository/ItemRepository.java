package com.toanyone.item.domain.repository;

import com.toanyone.item.domain.model.Item;
import com.toanyone.item.infrastructure.repository.ItemRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
}
