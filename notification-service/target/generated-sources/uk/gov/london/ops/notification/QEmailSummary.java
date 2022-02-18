package uk.gov.london.ops.notification;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QEmailSummary is a Querydsl query type for EmailSummary
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QEmailSummary extends EntityPathBase<EmailSummary> {

    private static final long serialVersionUID = -1015754541L;

    public static final QEmailSummary emailSummary = new QEmailSummary("emailSummary");

    public final NumberPath<Integer> attempts = createNumber("attempts", Integer.class);

    public final StringPath body = createString("body");

    public final DateTimePath<java.time.OffsetDateTime> date = createDateTime("date", java.time.OffsetDateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath recipient = createString("recipient");

    public final StringPath status = createString("status");

    public final StringPath subject = createString("subject");

    public QEmailSummary(String variable) {
        super(EmailSummary.class, forVariable(variable));
    }

    public QEmailSummary(Path<? extends EmailSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEmailSummary(PathMetadata metadata) {
        super(EmailSummary.class, metadata);
    }

}

