package com.toanyone.item.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "P_ITEM")
@SQLRestriction("deleted_at IS NULL")
public class Item extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private Integer price;

    @Column(nullable = false)
    private Integer stock;

    private String imageUrl;

    private Long storeId;

    public static Item create(String itemName, int price, int stock, String imageUrl, Long storeId) {
        Item item = new Item();
        item.itemName = itemName;
        item.price = price;
        item.stock = stock;
        item.imageUrl = imageUrl;
        item.storeId = storeId;
        return item;
    }

    public void updateItemName(String itemName) {
        this.itemName = itemName;
    }

    public void updatePrice(Integer price) {
        this.price = price;
    }

    //재고를 통 채로 바꾸는 메서드.
    public void updateStock(Integer stock) {
        this.stock = stock;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void addStock(Integer stock) {
        this.stock += stock;
    }

    public void reduceStock (Integer stock) {
        this.stock -= stock;
    }
}
