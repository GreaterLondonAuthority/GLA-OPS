/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api.project;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.project.NamedProjectBlock;
import uk.gov.london.ops.domain.project.Project;
import uk.gov.london.ops.domain.project.ProjectQuestionsBlock;
import uk.gov.london.ops.domain.user.Role;
import uk.gov.london.ops.exception.ApiError;
import uk.gov.london.ops.exception.ValidationException;

import javax.validation.Valid;
import java.util.List;

/**
 * Created by chris on 09/02/2017.
 */
@RestController
@RequestMapping("/api/v1")
@Api(
        description = "managing Project Questions data"
)
public class ProjectQuestionsAPI extends BaseProjectAPI {



    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/answers/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's answers", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectQuestionsBlock getProjectQuestionBlock(@PathVariable Integer id, @PathVariable Integer blockId) {
        Project fromDB = service.get(id);
        NamedProjectBlock toReturn = fromDB.getProjectBlockById(blockId);

        if (toReturn == null || !(toReturn instanceof ProjectQuestionsBlock)) {
            throw new ValidationException(String.format("Unable to find block: %d, on project: %d", blockId, id));
        }
        return (ProjectQuestionsBlock) toReturn;
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/questions", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's answers", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectQuestionsBlock> getProjectQuestionsBlocks(@PathVariable Integer id) {
        Project fromDB = service.get(id);
        List<ProjectQuestionsBlock> blocks = fromDB.getQuestionsBlocks();
        return blocks;
    }


    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.GLA_FINANCE, Role.GLA_READ_ONLY, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/questions/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's answers", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectQuestionsBlock getProjectQuestionsBlocks(@PathVariable Integer id, @PathVariable Integer blockId) {
        Project fromDB = service.get(id);
        List<ProjectQuestionsBlock> blocks = fromDB.getQuestionsBlocks();
        for (ProjectQuestionsBlock block : blocks) {
            if (block.getId().equals(blockId)) {
                return block;
            }
        }
        throw new ValidationException(String.format("Unable to find block with id: %d for project with id: %d", blockId, id));
    }

    @Secured({Role.OPS_ADMIN, Role.GLA_ORG_ADMIN, Role.GLA_SPM, Role.GLA_PM, Role.ORG_ADMIN, Role.PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/questions/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project's question block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectQuestionsBlock updateProjectQuestionBlock(@PathVariable Integer id,
                                                            @PathVariable Integer blockId,
                                                            @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
                                                            @Valid @RequestBody ProjectQuestionsBlock answers, BindingResult bindingResult) {
        verifyBinding("Invalid answers!", bindingResult);

        Project fromDB = service.get(id);

        fromDB = service.updateProjectAnswers(fromDB, blockId, answers, autosave);

        return (ProjectQuestionsBlock) fromDB.getProjectBlockById(blockId);
    }
}
