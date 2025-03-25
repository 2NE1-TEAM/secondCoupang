package com.toanyone.hub.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHubDistance is a Querydsl query type for HubDistance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHubDistance extends EntityPathBase<HubDistance> {

    private static final long serialVersionUID = -1919790528L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHubDistance hubDistance = new QHubDistance("hubDistance");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final NumberPath<Integer> distanceKm = createNumber("distanceKm", Integer.class);

    public final QHub endHub;

    public final NumberPath<Integer> estimatedTime = createNumber("estimatedTime", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QHub startHub;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QHubDistance(String variable) {
        this(HubDistance.class, forVariable(variable), INITS);
    }

    public QHubDistance(Path<? extends HubDistance> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHubDistance(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHubDistance(PathMetadata metadata, PathInits inits) {
        this(HubDistance.class, metadata, inits);
    }

    public QHubDistance(Class<? extends HubDistance> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.endHub = inits.isInitialized("endHub") ? new QHub(forProperty("endHub"), inits.get("endHub")) : null;
        this.startHub = inits.isInitialized("startHub") ? new QHub(forProperty("startHub"), inits.get("startHub")) : null;
    }

}

