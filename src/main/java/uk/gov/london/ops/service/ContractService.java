/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.london.ops.domain.template.Contract;
import uk.gov.london.ops.exception.NotFoundException;
import uk.gov.london.ops.exception.ValidationException;
import uk.gov.london.ops.repository.ContractRepository;

import java.util.List;

@Service
public class ContractService {

    @Autowired
    private ContractRepository contractRepository;

    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    public Contract find(Integer id) {
        Contract contract = contractRepository.findOne(id);
        if (contract == null) {
            throw new NotFoundException();
        }
        return contract;
    }

    public Contract create(Contract contract) {
        if (contract.getId() != null) {
            throw new ValidationException("contract ID should not be set!");
        }
        return contractRepository.save(contract);
    }

    public void update(Contract contract) {
        if (contract.getId() == null) {
            throw new ValidationException("contract should have an ID!");
        }
        contractRepository.save(contract);
    }

}
