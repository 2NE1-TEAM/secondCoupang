package com.toanyone.store.domain.model;

import lombok.Getter;

import java.io.Serializable;

@Getter
public enum StoreType implements Serializable {
    PRODUCER, CONSUMER;
}
