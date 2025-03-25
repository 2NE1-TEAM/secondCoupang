package com.toanyone.store.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDetailAddress is a Querydsl query type for DetailAddress
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QDetailAddress extends BeanPath<DetailAddress> {

    private static final long serialVersionUID = -1589851987L;

    public static final QDetailAddress detailAddress1 = new QDetailAddress("detailAddress1");

    public final StringPath detailAddress = createString("detailAddress");

    public QDetailAddress(String variable) {
        super(DetailAddress.class, forVariable(variable));
    }

    public QDetailAddress(Path<? extends DetailAddress> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDetailAddress(PathMetadata metadata) {
        super(DetailAddress.class, metadata);
    }

}

