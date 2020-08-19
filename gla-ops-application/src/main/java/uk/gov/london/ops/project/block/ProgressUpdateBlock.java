/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */

package uk.gov.london.ops.project.block;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity(name = "progress_update_block")
@DiscriminatorValue("PROGRESS_UPDATES")
public class ProgressUpdateBlock extends NamedProjectBlock {

    @Column(name = "progress_update")
    String progressUpdate;

    @Column(name = "private_update")
    String privateUpdate;

    @Column(name = "public_update")
    String publicUpdate;

    public String getProgressUpdate() {
        return progressUpdate;
    }

    public void setProgressUpdate(String progressUpdate) {
        this.progressUpdate = progressUpdate;
    }

    public String getPrivateUpdate() {
        return privateUpdate;
    }

    public void setPrivateUpdate(String privateUpdate) {
        this.privateUpdate = privateUpdate;
    }

    public String getPublicUpdate() {
        return publicUpdate;
    }

    public void setPublicUpdate(String publicUpdate) {
        this.publicUpdate = publicUpdate;
    }

    @Override
    public boolean isComplete() {
        if (!isVisited()) {
            return false;
        }
        if (StringUtils.isEmpty(progressUpdate)) {
            return false;
        }

        if (this.getProjectInterface().isMarkedForCorporate()) {
            if (StringUtils.isEmpty(publicUpdate)) {
                return false;
            }
            return !StringUtils.isEmpty(privateUpdate);
        }

        return true;
    }

    @Override
    protected void generateValidationFailures() {

    }

    @Override
    public void merge(NamedProjectBlock block) {
        ProgressUpdateBlock updatedBlock = (ProgressUpdateBlock) block;
        this.setProgressUpdate(updatedBlock.getProgressUpdate());
        this.setPrivateUpdate(updatedBlock.getPrivateUpdate());
        this.setPublicUpdate(updatedBlock.getPublicUpdate());
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock t) {
        final ProgressUpdateBlock target = (ProgressUpdateBlock) t;
        target.setProgressUpdate(this.progressUpdate);
        target.setPrivateUpdate(this.privateUpdate);
        target.setPublicUpdate(this.publicUpdate);
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }

}
