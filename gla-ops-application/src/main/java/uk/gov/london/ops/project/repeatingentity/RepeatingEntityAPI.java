/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiOperation;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public abstract class RepeatingEntityAPI<T extends RepeatingEntity> {

    @Autowired
    ObjectMapper objectMapper;

    RepeatingEntityService<T> service;

    public RepeatingEntityAPI(RepeatingEntityService<T> service) {
        this.service = service;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/project/{id}/block/{blockId}/item/", method = RequestMethod.POST)
    @ApiOperation(value = "create a repeating entity", notes = "")
    public T addRepeatingItem(@PathVariable Integer id, @PathVariable Integer blockId, @Valid @RequestBody String body)
            throws Exception {
        T value = getTypeFromJSON(body);
        return service.addRepeatingEntity(id, blockId, value);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/project/{id}/block/{blockId}/item/", method = RequestMethod.PUT)
    @ApiOperation(value = "update a repeating entity", notes = "")
    public T updateRepeatingItem(@PathVariable Integer id, @PathVariable Integer blockId, @Valid @RequestBody String body)
            throws Exception {
        T value = getTypeFromJSON(body);
        return service.updateRepeatingEntity(id, blockId, value);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/project/{id}/block/{blockId}/item/", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a repeating entry", notes = "")
    public void deleteRepeatingItem(@PathVariable Integer id, @PathVariable Integer blockId, @Valid @RequestBody String body)
            throws Exception {
        T value = getTypeFromJSON(body);
        service.deleteRepeatingEntity(id, blockId, value.getId());
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/project/{id}/block/{blockId}/item/{entityId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete a repeating entry", notes = "")
    public void deleteRepeatingItem(@PathVariable Integer id, @PathVariable Integer blockId, @PathVariable Integer entityId)
            throws Exception {
        service.deleteRepeatingEntity(id, blockId, entityId);
    }

    private T getTypeFromJSON(@RequestBody @Valid String body) throws java.io.IOException {
        Class<T> type = service.getEntityType();
        return objectMapper.readValue(body, type);
    }

}
