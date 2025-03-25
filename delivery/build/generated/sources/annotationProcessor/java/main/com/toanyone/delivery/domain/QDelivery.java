package com.toanyone.delivery.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDelivery is a Querydsl query type for Delivery
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDelivery extends EntityPathBase<Delivery> {

    private static final long serialVersionUID = 1026880368L;

    public static final QDelivery delivery = new QDelivery("delivery");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final NumberPath<Long> arrivalHubId = createNumber("arrivalHubId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final StringPath deliveryAddress = createString("deliveryAddress");

    public final ListPath<DeliveryRoad, QDeliveryRoad> deliveryRoads = this.<DeliveryRoad, QDeliveryRoad>createList("deliveryRoads", DeliveryRoad.class, QDeliveryRoad.class, PathInits.DIRECT2);

    public final EnumPath<Delivery.DeliveryStatus> deliveryStatus = createEnum("deliveryStatus", Delivery.DeliveryStatus.class);

    public final NumberPath<Long> departureHubId = createNumber("departureHubId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Long> orderId = createNumber("orderId", Long.class);

    public final StringPath recipient = createString("recipient");

    public final StringPath recipientSlackId = createString("recipientSlackId");

    public final NumberPath<Long> storeDeliveryManagerId = createNumber("storeDeliveryManagerId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QDelivery(String variable) {
        super(Delivery.class, forVariable(variable));
    }

    public QDelivery(Path<? extends Delivery> path) {
        super(path.getType(), path.getMetadata());
    }

    public QDelivery(PathMetadata metadata) {
        super(Delivery.class, metadata);
    }

}

