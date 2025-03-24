package com.toanyone.hub.domain.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QHub is a Querydsl query type for Hub
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QHub extends EntityPathBase<Hub> {

    private static final long serialVersionUID = 1595194667L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QHub hub = new QHub("hub");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final QAddress address;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final ListPath<HubDistance, QHubDistance> hubDistances = this.<HubDistance, QHubDistance>createList("hubDistances", HubDistance.class, QHubDistance.class, PathInits.DIRECT2);

    public final StringPath hubName = createString("hubName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLocation location;

    public final StringPath telephone = createString("telephone");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QHub(String variable) {
        this(Hub.class, forVariable(variable), INITS);
    }

    public QHub(Path<? extends Hub> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QHub(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QHub(PathMetadata metadata, PathInits inits) {
        this(Hub.class, metadata, inits);
    }

    public QHub(Class<? extends Hub> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.address = inits.isInitialized("address") ? new QAddress(forProperty("address")) : null;
        this.location = inits.isInitialized("location") ? new QLocation(forProperty("location")) : null;
    }

}

