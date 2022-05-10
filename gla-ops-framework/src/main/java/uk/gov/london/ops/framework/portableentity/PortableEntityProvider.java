/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.portableentity;

import java.io.IOException;

public interface PortableEntityProvider {
    boolean canHandleEntity(String className);

    String sanitize(String className, Integer id) throws IOException;

    void persist(String className, String json);
}
