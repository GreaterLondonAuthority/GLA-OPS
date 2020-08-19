/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

/**
 * Created by chris on 04/11/2019.
 */
public interface EntityCollection<T>  {

    T getNewEntityInstance();

    void createChildEntity(T child);

    boolean hasChildEntities();

}
