package com.toanyone.delivery.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QDeliveryManager is a Querydsl query type for DeliveryManager
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeliveryManager extends EntityPathBase<DeliveryManager> {

    private static final long serialVersionUID = 90764829L;

    public static final QDeliveryManager deliveryManager = new QDeliveryManager("deliveryManager");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final EnumPath<DeliveryManager.DeliveryManagerType> deliveryManagerType = createEnum("deliveryManagerType", DeliveryManager.DeliveryManagerType.class);

    public final NumberPath<Long> deliveryOrder = createNumber("deliveryOrder", Long.class);

    public final NumberPath<Long> hubId = createNumber("hubId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QDeliveryManager(String variable) {
        super(DeliveryManager.class, forVariable(variable));
    }

    public QDeliveryManager(Path<? extends DeliveryManager> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDeliveryManager(PathMetadata metadata) {
        super(DeliveryManager.class, metadata);
    }

}

