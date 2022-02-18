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
public interface CRUDServiceInterface<K, T> {

    T save(T entity);

    T find(K key);

    void delete(K key);

}
