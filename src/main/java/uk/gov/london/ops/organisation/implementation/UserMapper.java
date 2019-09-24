/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.ops.domain.organisation.Organisation;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.domain.user.User;
import uk.gov.london.ops.web.model.RoleModel;
import uk.gov.london.ops.web.model.UserModel;
import uk.gov.london.ops.web.model.UserRegistration;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;

@Component
public class UserMapper {

    @Autowired
    OrganisationDTOMapper mapper;

    public UserModel toModel(User user) {
        UserModel model = new UserModel();
        model.setUsername(user.getUsername());
        model.setFirstName(user.getFirstName());
        model.setLastName(user.getLastName());
        model.setRegisteredOn(user.getRegisteredOn());
        model.setLastLoggedOn(user.getLastLoggedOn());
        model.setApproved(user.isApproved());
        model.setFullName(user.getFirstName() + " " + user.getLastName());

        for (Organisation organisation: user.getOrganisations()) {
            model.getOrganisations().add(mapper.getOrganisationModelFromOrg(organisation));

        }

        String primaryRole = null;
        for (Role role: user.getRoles()) {
            RoleModel roleModel = toModel(role);
            if (OPS_ADMIN.equals(role.getName())) {
                primaryRole = UserModel.PRIMARY_ROLE_OPS_ADMIN;
            }
            model.getRoles().add(roleModel);
        }
        if (primaryRole == null) {
            if (user.isApproved()) {
                primaryRole = UserModel.PRIMARY_ROLE_PARTNER;
            } else {
                primaryRole = UserModel.PRIMARY_ROLE_USER;
            }
        }
        model.setPrimaryRole(primaryRole);

        return model;
    }

    public List<UserModel> mapToModel(Collection<User> users) {
        if (users == null) {
            return null;
        }
        else {
            return users.stream().map(this::toModel).collect(Collectors.toCollection(LinkedList::new));
        }
    }

    public RoleModel toModel(Role role) {
        RoleModel model = new RoleModel();
        model.setName(role.getName());
        model.setDescription(role.getDescription());
        model.setOrganisationId(role.getOrganisation().getId());
        model.setApproved(role.isApproved());
        model.setApprovedBy(role.getApprovedBy());
        model.setApprovedOn(role.getApprovedOn());
        model.setManagingOrganisationId(role.getOrganisation().getManagingOrganisationId());
        model.setOrgStatus(role.getOrganisation().getStatus());
        return model;
    }

    public User toEntity(UserRegistration registration, Organisation organisation) {
        User user = new User(registration.getEmail().toLowerCase(), registration.getPassword());
        user.setFirstName(registration.getFirstName());
        user.setLastName(registration.getLastName());
        user.setPhoneNumber(registration.getPhoneNumber());
        user.setRegisteredOn(new Date());
        user.addUnapprovedRole(Role.getDefaultForOrganisation(organisation), organisation);
        return user;
    }
}
