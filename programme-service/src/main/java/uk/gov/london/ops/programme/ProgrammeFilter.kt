/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.programme

import java.util.*

class ProgrammeFilterItem(var id: Int, var name: String, var status: String, var enabled: Boolean, var managingOrganisationId: Int, var managingOrganisationName: String) {

    var templates: List<TemplateFilterItem> = ArrayList()

}

class TemplateFilterItem(var id: Int, var name: String)

