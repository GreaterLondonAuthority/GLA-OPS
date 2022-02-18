package uk.gov.london.common.organisation;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QBaseOrganisation is a Querydsl query type for BaseOrganisation
 */
@Generated("com.querydsl.codegen.EmbeddableSerializer")
public class QBaseOrganisation extends BeanPath<BaseOrganisation> {

    private static final long serialVersionUID = 299830682L;

    public static final QBaseOrganisation baseOrganisation = new QBaseOrganisation("baseOrganisation");

    public final StringPath externalReference = createString("externalReference");

    public QBaseOrganisation(String variable) {
        super(BaseOrganisation.class, forVariable(variable));
    }

    public QBaseOrganisation(Path<? extends BaseOrganisation> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBaseOrganisation(PathMetadata metadata) {
        super(BaseOrganisation.class, metadata);
    }

}

