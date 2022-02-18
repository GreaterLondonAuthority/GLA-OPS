/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project;

public class ProjectsTransferResult {

    private final int nbTransferred;
    private final int nbErrors;

    public ProjectsTransferResult(int nbTransferred, int nbErrors) {
        this.nbTransferred = nbTransferred;
        this.nbErrors = nbErrors;
    }

    public int getNbTransferred() {
        return nbTransferred;
    }

    public int getNbErrors() {
        return nbErrors;
    }

}
