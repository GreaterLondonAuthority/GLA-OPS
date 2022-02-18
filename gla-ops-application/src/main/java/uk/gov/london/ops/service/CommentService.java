/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.Comment;
import uk.gov.london.ops.framework.EntityType;
import uk.gov.london.ops.repository.CommentRepository;
import uk.gov.london.ops.user.UserService;

import javax.transaction.Transactional;

@Transactional
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;

    public CommentService(CommentRepository commentRepository, UserService userService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
    }

    public Page<Comment> getComments(EntityType entityType, Integer entityId, Pageable pageable) {
        Page<Comment> comments = commentRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable);
        for (Comment comment: comments.getContent()) {
            enrich(comment);
        }
        return comments;
    }

    private void enrich(Comment comment) {
        userService.enrich(comment);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

}
