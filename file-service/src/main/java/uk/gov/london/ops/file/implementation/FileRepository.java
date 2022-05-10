/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.file.implementation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileCategory;

import java.sql.Blob;
import java.util.List;
import java.util.Set;

public interface FileRepository extends JpaRepository<AttachmentFile, Integer> {

    @Query(value = "select file_content from file where id = ?1", nativeQuery = true)
    Blob getFileContent(Integer id);

    @Modifying
    @Query(value = "update file set file_content = ?2 where id = ?1", nativeQuery = true)
    void updateFileContent(Integer id, byte[] fileContent);

    @Query(value = "select f.id, f.content_type, f.file_name, f.created_on, f.created_by, "
            + "f.organisation_id, f.file_size, f.category, f.storage_location, f.link "
            + "from V_project_block_latest l "
            + "inner join project_block_question pbq on l.block_id = pbq.project_block_id and block_type = 'QUESTIONS'  "
            + "inner join template_question tq on tq.id = pbq.question_id  "
            + "inner join question q on q.id = tq.question_id and q.answer_type = 'FileUpload' "
            + "inner join answer a on a.question_id = q.id and a.questions_block =pbq.project_block_id "
            + "inner join answer_attachment aa on a.id = aa.answer_id   "
            + "inner join file f on f.id = aa.attachment_id "
            + "where l.project_id = ?1", nativeQuery = true)
    Set<AttachmentFile> getAllAttachmentsForProject(Integer projectId);

    AttachmentFile findByFileNameAndCategory(String iconName, FileCategory icon);

    List<AttachmentFile> findAllByCategory(FileCategory icon);
}
