/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.refdata.implementation.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.london.common.CSVFile;
import uk.gov.london.ops.framework.di.DataInitialiserModule;
import uk.gov.london.ops.refdata.BoroughEntity;
import uk.gov.london.ops.refdata.WardEntity;
import uk.gov.london.ops.refdata.implementation.repository.BoroughRepository;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
@Component
public class BoroughDataInitialiser implements DataInitialiserModule {

    Logger log = LoggerFactory.getLogger(getClass());

    private static final String MULTIPLE_BOROUGHS_NAME = "Multiple London Boroughs";

    @Autowired
    private BoroughRepository boroughRepository;

    @Override
    public String getName() {
        return "Borough data initialiser";
    }

    @Override
    public boolean runInAllEnvironments() {
        return true;
    }

    @Override
    public void addReferenceData() {
        int id = 1;
        int displayOrder = 1;

        if (boroughRepository.count() > 0) {
            return;
        }

        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Barking and Dagenham"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Barnet"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Bexley"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Brent"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Bromley"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Camden"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "City of London"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Croydon"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Ealing"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Enfield"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Greenwich"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Hackney"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Hammersmith and Fulham"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Haringey"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Harrow"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Havering"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Hillingdon"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Hounslow"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Islington"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Kensington and Chelsea"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Kingston upon Thames"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Lambeth"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Lewisham"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Merton"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Newham"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Redbridge"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Richmond upon Thames"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Southwark"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Sutton"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Tower Hamlets"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Waltham Forest"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Wandsworth"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, "Westminster"));
        boroughRepository.save(new BoroughEntity(id++, displayOrder++, MULTIPLE_BOROUGHS_NAME));
        assignWards();
    }

    @Override
    public int executionOrder() {
        return 5;
    }

    private void assignWards() {
        int id = 1;
        try {
            CSVFile csvFile = new CSVFile(getClass().getResourceAsStream("llr_wards.csv"));

            Map<String, BoroughEntity> boroughs = new HashMap<>();
            List<BoroughEntity> all = boroughRepository.findAll();
            for (BoroughEntity borough : all) {
                boroughs.put(borough.getBoroughName(), borough);
            }

            while (csvFile.nextRow()) {
                String borough = csvFile.getString("Borough name");
                String ward = csvFile.getString("Ward name");
                BoroughEntity boroughToUpdate = boroughs.get(borough);
                if (boroughToUpdate != null) {
                    boroughToUpdate.getWards().add(new WardEntity(id++, boroughToUpdate.getWards().size() + 1, ward));
                } else {
                    log.error(String.format("Unable fo find borough with name %s for ward %s, row %d", borough, ward,
                            csvFile.getRowIndex()));
                }
            }

            //Add extra 'Multiple London wards' option which is not in csv file
            BoroughEntity boroughToUpdate = boroughs.get(MULTIPLE_BOROUGHS_NAME);
            boroughs.get(MULTIPLE_BOROUGHS_NAME).getWards()
                    .add(new WardEntity(id++, boroughToUpdate.getWards().size() + 1, "Multiple London wards"));

            boroughRepository.saveAll(boroughs.values());

        } catch (Exception e) {
            log.error("Error loading LLR Wards", e);
        }
    }

}
