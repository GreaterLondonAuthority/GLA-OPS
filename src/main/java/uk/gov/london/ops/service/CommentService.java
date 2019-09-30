/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.Comment;
import uk.gov.london.ops.domain.EntityType;
import uk.gov.london.ops.repository.CommentRepository;

import javax.transaction.Transactional;

@Transactional
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    public Page<Comment> getComments(EntityType entityType, Integer entityId, Pageable pageable) {
        return commentRepository.findAllByEntityTypeAndEntityId(entityType, entityId, pageable);
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }


    public void deleteComment(Integer commentId) {
        commentRepository.deleteById(commentId);
    }

}
