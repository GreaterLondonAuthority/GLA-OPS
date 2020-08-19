/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.dto;

import org.springframework.stereotype.Component;
import uk.gov.london.ops.organisation.model.OrganisationGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class OrganisationGroupMapper {

    public List<OrganisationGroupModel> mapToModel(Set<OrganisationGroup> entities) {
        List<OrganisationGroupModel> models = new ArrayList<>();
        for (OrganisationGroup group : entities) {
            OrganisationGroupModel groupModel = new OrganisationGroupModel();
            groupModel.setId(group.getId());
            groupModel.setName(group.getName());
            groupModel.setLeadOrgName(group.getLeadOrganisation().getName());
            models.add(groupModel);
        }
        return models;
    }

}
