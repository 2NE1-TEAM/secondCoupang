package com.toanyone.delivery.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDeliveryRoad is a Querydsl query type for DeliveryRoad
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDeliveryRoad extends EntityPathBase<DeliveryRoad> {

    private static final long serialVersionUID = -371937616L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDeliveryRoad deliveryRoad = new QDeliveryRoad("deliveryRoad");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<java.math.BigDecimal> actualDistance = createNumber("actualDistance", java.math.BigDecimal.class);

    public final NumberPath<Integer> actualDuration = createNumber("actualDuration", Integer.class);

    public final NumberPath<Long> arrivalHubId = createNumber("arrivalHubId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final EnumPath<DeliveryRoad.CurrentStatus> currentStatus = createEnum("currentStatus", DeliveryRoad.CurrentStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final QDelivery delivery;

    public final NumberPath<Long> deliveryManagerId = createNumber("deliveryManagerId", Long.class);

    public final NumberPath<Long> departureHubId = createNumber("departureHubId", Long.class);

    public final NumberPath<java.math.BigDecimal> estimatedDistance = createNumber("estimatedDistance", java.math.BigDecimal.class);

    public final NumberPath<Integer> estimatedDuration = createNumber("estimatedDuration", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> sequence = createNumber("sequence", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QDeliveryRoad(String variable) {
        this(DeliveryRoad.class, forVariable(variable), INITS);
    }

    public QDeliveryRoad(Path<? extends DeliveryRoad> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDeliveryRoad(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDeliveryRoad(PathMetadata metadata, PathInits inits) {
        this(DeliveryRoad.class, metadata, inits);
    }

    public QDeliveryRoad(Class<? extends DeliveryRoad> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.delivery = inits.isInitialized("delivery") ? new QDelivery(forProperty("delivery")) : null;
    }

}

