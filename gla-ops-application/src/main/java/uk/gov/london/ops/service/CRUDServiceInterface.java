/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

/**
 * Interface for Service implementations supporting a basic Create/Read/Update/Delete model.
 */
public interface CRUDServiceInterface<KeyType,EntityType> {

    EntityType save(EntityType entity);
    EntityType find(KeyType key);
    void delete(KeyType key);

}
