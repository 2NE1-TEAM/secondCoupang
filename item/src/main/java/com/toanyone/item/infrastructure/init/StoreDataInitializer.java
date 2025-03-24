package com.toanyone.item.infrastructure.init;

import com.toanyone.item.domain.model.Item;
import com.toanyone.item.domain.repository.ItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class StoreDataInitializer {

    private final ItemRepository itemRepository;

    @PostConstruct
    @Transactional
    public void init() {
        insertItem();
    }

    private void insertItem() {
        for (int i = 1; i <= 100; i++) {
            String itemName = "아이템 " + i;
            int price = (int) (Math.random() * 9000) + 1000; // 1000 ~ 9999
            int stock = (int) (Math.random() * 91) + 10;     // 10 ~ 100
            String imageUrl = "https://s3.com/item" + i + ".jpg";
            Long storeId = (long) ((i - 1) / 10 + 1);        // 1L ~ 10L: 10개씩 분배

            Item item = Item.create(itemName, price, stock, imageUrl, storeId);
            itemRepository.save(item);
        }
    }
}