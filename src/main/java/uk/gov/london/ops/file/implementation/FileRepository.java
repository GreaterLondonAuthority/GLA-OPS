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

public interface FileRepository extends JpaRepository<AttachmentFile, Integer> {


    @Query(value = "select file_content from file where id = ?1", nativeQuery = true)
    byte[] getFileContent(Integer id);


    @Modifying
    @Query(value = "update file set file_content = ?2 where id = ?1",  nativeQuery = true)
    void updateFileContent(Integer id, byte[] fileContent);



}
