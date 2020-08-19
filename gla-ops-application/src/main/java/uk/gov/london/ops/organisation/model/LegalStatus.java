/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.organisation.model;

/**
 * Created by cmatias on 06/05/2020.
 */
public enum LegalStatus {
  LONDON_BOROUGH(1, "LONDON_BOROUGH", "London Borough"),
  EDUCATIONAL(2, "EDUCATIONAL", "Educational or College Establishment"),
  HOUSING_ASSOCIATION(3, "HOUSING_ASSOCIATION", "Housing Association"),
  REGISTERED_CHARITY(4, "REGISTERED_CHARITY", "Registered Charity"),
  UNREGISTERED_CHARITY(5, "UNREGISTERED_CHARITY", "Unregistered Charity"),
  PUBLIC_LIMITED_COMPANY(6, "PUBLIC_LIMITED_COMPANY", "Public Limited Company"),
  PRIVATE_LIMITED_COMPANY(7, "PRIVATE_LIMITED_COMPANY", "Private Limited Company"),
  PARTNERSHIP(8, "PARTNERSHIP", "Partnership"),
  LIMITED_LIABILITY(9,  "LIMITED_LIABILITY", "Limited Liability Partnership"),
  SOLE_TRADER(10, "SOLE_TRADER", "Sole Trader"),
  ALMSHOUSE(11, "ALMSHOUSE", "Almshouse Organisation"),
  UNINCORPORATED_COMMUNITY(12, "UNINCORPORATED_COMMUNITY", "Unincorporated Community Organisation"),
  UNINCORPORATED_SPORTS(13, "UNINCORPORATED_SPORTS", "Unincorporated Sports Association"),
  TRUST(14, "TRUST", "Trust"),
  OTHER(15, "OTHER", "Other");

  private final int id;
  private final String name;
  private final String description;

  LegalStatus(int id, String name, String description) {
    this.id = id;
    this.name = name;
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    if (null == description || description.isEmpty()) {
      return name;
    }
    return description;
  }
}