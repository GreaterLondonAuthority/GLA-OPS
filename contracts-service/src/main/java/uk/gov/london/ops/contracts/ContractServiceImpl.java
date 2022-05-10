/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.contracts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.audit.AuditService;
import uk.gov.london.ops.framework.enums.ContractWorkflowType;
import uk.gov.london.ops.framework.exception.NotFoundException;
import uk.gov.london.ops.framework.exception.ValidationException;
import uk.gov.london.ops.organisation.OrganisationService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ContractServiceImpl implements ContractService {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractTemplatesEntityRepository contractTemplatesEntityRepository;

    @Autowired
    private OrganisationService organisationService;

    @Autowired
    private AuditService auditService;

    @Override
    public List<ContractModel> findAll() {
        return contractRepository.findAll().stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public List<ContractTemplatesModel> findAllWithTemplates() {
        return contractTemplatesEntityRepository.findAll().stream().map(this::toModel).collect(Collectors.toList());
    }

    @Override
    public ContractModel find(Integer id) {
        ContractModel contractModel = findById(id);
        if (contractModel == null) {
            throw new NotFoundException();
        }
        return contractModel;
    }

    @Override
    public ContractModel findByName(String name) {
        return contractRepository.findByName(name).map(this::toModel).orElse(null);
    }

    @Override
    public ContractModel create(ContractModel contractModel) {
        if (contractModel.getId() != null) {
            throw new ValidationException("contract ID should not be set for a new contract");
        }
        ContractModel existing = findByName(contractModel.getName());
        if (existing != null) {
            throw new ValidationException(String.format("a contract with name %s already exists, please use another " +
                    "name or set the contractId field to %d and use this existing contract", existing.getName(), existing.getId()));
        }
        return toModel(contractRepository.save(new ContractEntity(contractModel)));
    }

    @Override
    public void update(ContractModel contractModel) {
        if (contractModel.getId() == null) {
            throw new ValidationException("contract should have an ID!");
        }
        contractRepository.save(new ContractEntity(contractModel));
    }

    public void updateAll(Collection<ContractModel> contractModels) {
        Collection<ContractEntity> contractEntities = contractModels.stream().map(ContractEntity::new).collect(Collectors.toSet());
        contractRepository.saveAll(contractEntities);
    }

    @Override
    public ContractModel findById(Integer id) {
        return contractRepository.findById(id).map(this::toModel).orElse(null);
    }

    @Override
    public void updateContractWorkflowType(Integer contractId, ContractWorkflowType contractWorkflowType) {
        ContractModel contractModel = findById(contractId);
        if (contractModel == null) {
            throw new ValidationException("ContractEntity ID is not correct");
        }

        if (contractModel.getContractWorkflowType() != contractWorkflowType) {
            organisationService.validateContractUsage(contractId);
            String auditMessage = String.format("ContractEntity: %d, work flow type has been changed from: %s to: %s",
                contractId, contractModel.getContractWorkflowType().name(), contractWorkflowType.name());
            contractModel.setContractWorkflowType(contractWorkflowType);
            contractRepository.save(new ContractEntity(contractModel));
            auditService.auditCurrentUserActivity(auditMessage);
        }
    }

    ContractModel toModel(ContractEntity entity) {
        return new ContractModel(entity.getId(), entity.getName(), entity.getContractWorkflowType());
    }

    ContractTemplatesModel toModel(ContractTemplatesEntity entity) {
        return new ContractTemplatesModel(entity.getId(), entity.getName(), entity.getContractWorkflowType(), entity.getTemplateList());
    }

}
