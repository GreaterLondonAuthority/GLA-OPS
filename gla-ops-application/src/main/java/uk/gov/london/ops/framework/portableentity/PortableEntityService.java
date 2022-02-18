/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.portableentity;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class PortableEntityService {

    @Autowired
    Set<PortableEntityProvider> providers;

    public String getSanitisedEntity(String className, int id) throws IOException {
        return Objects.requireNonNull(providers.stream()
                .filter(p -> p.canHandleEntity(className))
                .findFirst().orElse(null))
                .sanitize(className, id);
    }

    public void saveSanitisedEntity(String className, String json) {
        Objects.requireNonNull(providers.stream()
                .filter(p -> p.canHandleEntity(className))
                .findFirst().orElse(null))
                .persist(className, json);
    }

}
