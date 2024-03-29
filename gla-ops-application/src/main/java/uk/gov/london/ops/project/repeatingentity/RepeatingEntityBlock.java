/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.repeatingentity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Transient;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.project.block.NamedProjectBlock;
import uk.gov.london.ops.project.block.ProjectBlockType;
import uk.gov.london.ops.project.block.ProjectDifferences;
import uk.gov.london.ops.project.template.domain.RepeatingEntityTemplateBlock;
import uk.gov.london.ops.project.template.domain.TemplateBlock;

/**
 * Created by chris on 04/11/2019.
 */
@Entity
public abstract class RepeatingEntityBlock<T extends RepeatingEntity> extends NamedProjectBlock implements EntityCollection<T> {

    @Column
    private boolean blockRequired = true;

    public RepeatingEntityBlock() {
        setBlockType(getProjectBlockType());
    }

    public abstract ProjectBlockType getProjectBlockType();

    @JsonIgnore
    public abstract List<T> getRepeatingEntities();

    public abstract String getRootPath();

    public void setRepeatingEntities(List<T> repeatingEntities) {
        this.getRepeatingEntities().clear();
        this.getRepeatingEntities().addAll(repeatingEntities);

    }

    public T getLastEntry() {
        List<T> repeatingEntities = getRepeatingEntities();
        if (repeatingEntities != null && repeatingEntities.size() > 0) {
            return repeatingEntities.get(repeatingEntities.size() - 1);
        }
        return null;
    }

    public T getEntry(int id) {
        return getRepeatingEntities().stream().filter(a -> a.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ValidationException("Unable to find entry with id " + id));
    }

    public void createNewEntity(T entity) {
        if (entity.getId() != null) {
            throw new ValidationException("Must not specify an ID for create");
        }
        this.getRepeatingEntities().add(entity);
    }

    public void updateExistingEntity(T changedEntity) {
        if (changedEntity.getId() == null) {
            throw new ValidationException("Must specify an ID for update");
        }
        T existing = this.getEntry(changedEntity.getId());
        existing.update(changedEntity);
    }

    public void delete(Integer id) {
        if (id == null) {
            throw new ValidationException("Must specify an ID for delete");
        }
        if (!getRepeatingEntities().removeIf(e -> e.getId().equals(id))) {
            throw new ValidationException("Unable to find ID to remove");
        }
    }

    @Override
    public void merge(NamedProjectBlock namedProjectBlock) {
        // override in subclasses to store other specific entity data
        if (namedProjectBlock instanceof RepeatingEntityBlock) {
            RepeatingEntityBlock block = (RepeatingEntityBlock) namedProjectBlock;
            this.setBlockRequired(block.getBlockRequired());
        }
    }

    @Override
    @Transient
    protected void generateValidationFailures() {

        if (!this.isComplete()) {
            this.addErrorMessage("Block1", "", "Please ensure all mandatory fields are entered");
        }
    }

    @JsonIgnore
    public boolean passesConditions() {
        return isVisited()
                && getRepeatingEntities() != null
                && !getRepeatingEntities().isEmpty()
                && getRepeatingEntities().stream().allMatch(RepeatingEntity::isComplete)
                && getValidationFailures().size() == 0;
    }


    @Transient
    @Override
    public boolean isComplete() {
        return isNotRequired() || (
                    isVisited()
                    && getRepeatingEntities() != null
                    && !getRepeatingEntities().isEmpty()
                    && getRepeatingEntities().stream().allMatch(RepeatingEntity::isComplete)
                    && getValidationFailures().size() == 0
                );
    }

    @JsonIgnore
    public boolean isNotRequired() {
        if (project == null) {
            return false;
        }
        TemplateBlock templateBlock = this.project.getTemplate().getSingleBlockByType(getProjectBlockType());
        if (templateBlock instanceof RepeatingEntityTemplateBlock) {
            RepeatingEntityTemplateBlock repeatingEntityTemplateBlock = (RepeatingEntityTemplateBlock) templateBlock;
            return repeatingEntityTemplateBlock.getHasBlockRequiredOption() && !getBlockRequired();
        }
        return false;
    }

    @Override
    protected void copyBlockContentInto(NamedProjectBlock target) {
        super.copyBlockContentInto(target);
        RepeatingEntityBlock otherBlock = (RepeatingEntityBlock) target;

        List otherEntities = otherBlock.getRepeatingEntities();

        for (T repeatingEntity : this.getRepeatingEntities()) {
            RepeatingEntity copied = repeatingEntity.copy();
            otherEntities.add(copied);
        }

    }

    @Override
    protected void compareBlockSpecificContent(NamedProjectBlock other, ProjectDifferences differences) {
    }

    @Override
    public boolean isBlockRevertable() {
        return true;
    }



    public void createChildEntity(T child) {
        this.getRepeatingEntities().add(child);
    }

    public boolean hasChildEntities() {
        return this.getRepeatingEntities() != null && !this.getRepeatingEntities().isEmpty();
    }

    public boolean getBlockRequired() {
        return blockRequired;
    }

    public void setBlockRequired(boolean blockRequired) {
        this.blockRequired = blockRequired;
    }


}
