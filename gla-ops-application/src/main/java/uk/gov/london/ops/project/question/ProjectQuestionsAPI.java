/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.question;

import static uk.gov.london.common.user.BaseRole.GLA_FINANCE;
import static uk.gov.london.common.user.BaseRole.GLA_ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.GLA_PM;
import static uk.gov.london.common.user.BaseRole.GLA_READ_ONLY;
import static uk.gov.london.common.user.BaseRole.GLA_SPM;
import static uk.gov.london.common.user.BaseRole.OPS_ADMIN;
import static uk.gov.london.common.user.BaseRole.ORG_ADMIN;
import static uk.gov.london.common.user.BaseRole.PROJECT_EDITOR;
import static uk.gov.london.common.user.BaseRole.PROJECT_READER;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.london.common.error.ApiError;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.NamedProjectBlock;

/**
 * Created by chris on 09/02/2017.
 */
@RestController
@RequestMapping("/api/v1")
@Api("managing Project Questions data")
public class ProjectQuestionsAPI {

    @Autowired
    private ProjectQuestionsService questionsService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/answers/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's answers", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectQuestionsBlock getProjectQuestionBlock(@PathVariable Integer id, @PathVariable Integer blockId) {
        Project fromDB = questionsService.get(id);
        NamedProjectBlock toReturn = fromDB.getProjectBlockById(blockId);

        if (toReturn == null || !(toReturn instanceof ProjectQuestionsBlock)) {
            throw new ValidationException(String.format("Unable to find block: %d, on project: %d", blockId, id));
        }
        return (ProjectQuestionsBlock) toReturn;
    }


    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/questions", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's answers", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public List<ProjectQuestionsBlock> getProjectQuestionsBlocks(@PathVariable Integer id) {
        Project fromDB = questionsService.get(id);
        List<ProjectQuestionsBlock> blocks = fromDB.getQuestionsBlocks();
        return blocks;
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER})
    @RequestMapping(value = "/projects/{id}/questions/{blockId}", method = RequestMethod.GET)
    @ApiOperation(value = "get a project's answers", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectQuestionsBlock getProjectQuestionsBlocks(@PathVariable Integer id, @PathVariable Integer blockId) {
        Project fromDB = questionsService.get(id);
        List<ProjectQuestionsBlock> blocks = fromDB.getQuestionsBlocks();
        for (ProjectQuestionsBlock block : blocks) {
            if (block.getId().equals(blockId)) {
                return block;
            }
        }
        throw new ValidationException(String.format("Unable to find block with id: %d for project with id: %d", blockId, id));
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, ORG_ADMIN, PROJECT_EDITOR})
    @RequestMapping(value = "/projects/{id}/questions/{blockId}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a project's question block", notes = "")
    @ApiResponses(@ApiResponse(code = 400, message = "validation error", response = ApiError.class))
    public ProjectQuestionsBlock updateProjectQuestionBlock(@PathVariable Integer id,
            @PathVariable Integer blockId,
            @RequestParam(name = "autosave", defaultValue = "false", required = false) boolean autosave,
            @Valid @RequestBody ProjectQuestionsBlock answers, BindingResult bindingResult) {
        verifyBinding("Invalid answers!", bindingResult);
        return questionsService.updateProjectAnswers(id, blockId, answers, autosave);
    }
}
