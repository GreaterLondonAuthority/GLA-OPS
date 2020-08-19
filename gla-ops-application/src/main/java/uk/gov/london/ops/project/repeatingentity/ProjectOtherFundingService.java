/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.file.AttachmentFile;
import uk.gov.london.ops.file.FileService;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.Project;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.template.domain.OtherFundingTemplateBlock;

@Service
/**
 * service for managing Project Other Funding block
 */
public class ProjectOtherFundingService extends RepeatingEntityService<OtherFunding> {

    @Autowired
    private FileService fileService;

    @Autowired
    private AuditService auditService;

    @Override
    public Class<OtherFunding> getEntityType() {
        return OtherFunding.class;
    }

    public OtherFundingBlock attachEvidence(Integer projectId, Integer blockId, Integer otherFundingId, Integer fileId,
            boolean releaseLock) {
        Project project = get(projectId);

        OtherFundingBlock otherFundingBlock = getBlock(project, blockId, OtherFundingBlock.class);

        checkForLock(otherFundingBlock);

        OtherFundingTemplateBlock otherFundingTemplateBlock = (OtherFundingTemplateBlock) project.getTemplate()
                .getSingleBlockByType(ProjectBlockType.OtherFunding);
        Integer maxEvidenceAttachments = otherFundingTemplateBlock.getMaxEvidenceAttachments();
        OtherFunding existingOtherFunding = otherFundingBlock.getOtherFundingById(otherFundingId);
        if (maxEvidenceAttachments != null && maxEvidenceAttachments.equals(existingOtherFunding.getAttachments().size())) {
            throw new ValidationException("Unable to add more attachments as the limit has been reached.");
        }

        AttachmentFile file = fileService.get(fileId);
        existingOtherFunding.getAttachments().add(file);
        releaseOrRefreshLock(otherFundingBlock, releaseLock);
        project = updateProject(project);

        return project.getOtherFundingBlock();
    }

    public OtherFundingBlock removeEvidence(Integer projectId, Integer blockId, Integer otherFundingId, Integer fileId,
            boolean releaseLock) {
        Project project = get(projectId);

        OtherFundingBlock otherFundingBlock = getBlock(project, blockId, OtherFundingBlock.class);

        checkForLock(otherFundingBlock);

        OtherFunding existingOtherFunding = otherFundingBlock.getOtherFundingById(otherFundingId);

        if (existingOtherFunding == null) {
            throw new ValidationException("Requested other funding not found.");
        }

        Optional<AttachmentFile> first = existingOtherFunding.getAttachments().stream().filter(m -> m.getId().equals(fileId))
                .findFirst();
        if (first.isPresent()) {
            AttachmentFile attachment = first.get();
            existingOtherFunding.getAttachments().remove(attachment);
            auditService.auditCurrentUserActivity(
                    "Attachment " + attachment.getFileName() + " was deleted from Milestone " + otherFundingId + " on project: "
                            + project.getId());
        } else {
            throw new ValidationException("Unable to remove attachment with specified id.");
        }

        releaseOrRefreshLock(otherFundingBlock, releaseLock);
        project = updateProject(project);
        return project.getOtherFundingBlock();
    }
}
