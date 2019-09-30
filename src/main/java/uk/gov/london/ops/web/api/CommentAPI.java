/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.ops.domain.Comment;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.service.CommentService;

import javax.validation.Valid;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.web.APIUtils.verifyBinding;

@RestController
@RequestMapping("/api/v1")
@Api(description="comments api")
public class CommentAPI {

    @Autowired
    private CommentService commentService;

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, TECH_ADMIN})
    @RequestMapping(value = "/comments", method = RequestMethod.GET)
    @ApiOperation(value="gets the list of comments", notes="gets the list of comments")
    public @ResponseBody Page<Comment> getComments(@RequestParam EntityType entityType, @RequestParam Integer entityId, Pageable pageable) {
        return commentService.getComments(entityType, entityId, pageable);
    }

    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE})
    @RequestMapping(value = "/comments", method = RequestMethod.POST)
    @ApiOperation(value="creates a comment", notes="creates a comment")
    public @ResponseBody Comment createComment(@Valid @RequestBody Comment comment, BindingResult bindingResult) {
        verifyBinding("Invalid comment data!", bindingResult);
        return commentService.createComment(comment);
    }

    @Secured({OPS_ADMIN})
    @RequestMapping(value = "/comments/{commentId}", method = RequestMethod.DELETE)
    @ApiOperation(value="deletes a comment", notes="deletes a comment")
    public void deleteComment(@PathVariable Integer commentId) {
        commentService.deleteComment(commentId);
    }

}
