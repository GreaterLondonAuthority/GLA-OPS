/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.project.skills;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import uk.gov.london.common.skills.FundingRecord;
import uk.gov.london.common.skills.SkillsGrantType;
import uk.gov.london.ops.framework.calendar.AcademicCalendar;

import java.util.List;

import static uk.gov.london.common.user.BaseRole.*;
import static uk.gov.london.ops.framework.OPSUtils.verifyBinding;
import static uk.gov.london.ops.user.User.SGW_SYSTEM_USER;

@RestController
@RequestMapping("/api/v1")
@Api("API for managing Skills Profile Data")
public class SkillsAPI {

    @Autowired
    SkillsService skillsService;

    @Autowired
    AcademicCalendar academicCalendar;

    // temp mechanism to track number of calls from ILR since start up for e2e testing purposes
    private int nbCallsFromIlr = 0;

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/skills/paymentProfiles", method = RequestMethod.GET)
    @ApiOperation(value = "Get all payment profiles", notes = "Get all payment profiles")
    public List<SkillsPaymentProfile> getAllPaymentProfiles() {
        return skillsService.getSkillsPaymentProfiles();
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/skills/paymentProfiles/{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "updates a single payment percentage by id", notes = "updates a single payment percentage by id")
    public void updatePaymentProfile(@PathVariable Integer id, @RequestBody SkillsPaymentProfile profile,
            BindingResult bindingResult) {
        verifyBinding("Invalid SkillsPaymentProfile type!", bindingResult);
        skillsService.updateSkillsPaymentProfile(id, profile);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/skills/paymentProfiles/{type}/{year}", method = RequestMethod.DELETE)
    @ApiOperation(value = "delete all existing payment profile entries by type and year",
            notes = "deletes all existing skills payment profile entries by type and year")
    public void deletePaymentProfile(@PathVariable SkillsGrantType type, @PathVariable Integer year) {
        skillsService.deleteSkillsPaymentProfileByTypeAndYear(type, year);
    }

    @Secured(OPS_ADMIN)
    @RequestMapping(value = "/skills/paymentProfiles/{type}", method = RequestMethod.POST)
    @ApiOperation(value = "clones a previous year data or creates new year data",
            notes = "clones a previous year data or creates new year data")
    public List<SkillsPaymentProfile> createOrCloneYear(@PathVariable String type) {
        return skillsService.createOrCloneYear(SkillsGrantType.valueOf(type));
    }

    @PreAuthorize("authentication.name == '" + SGW_SYSTEM_USER + "'")
    @RequestMapping(value = "/skills/getNbCallsFromIlr", method = RequestMethod.GET)
    public Integer getNbCallsFromIlr() {
        return nbCallsFromIlr;
    }

    @PreAuthorize("authentication.name == '" + SGW_SYSTEM_USER + "'")
    @RequestMapping(value = "/skills/fundingSummary", method = RequestMethod.POST)
    public void handleILRFundingSummary(@RequestBody List<FundingRecord> fundingRecords,
            @RequestParam("academicYear") Integer academicYear,
            @RequestParam("period") Integer period) {
        nbCallsFromIlr++;
        skillsService.handleILRFundingSummary(fundingRecords, academicYear, period);
    }

    @RequestMapping(value = "/skills/currentAcademicYear", method = RequestMethod.GET)
    @ApiOperation(value = "returns current academic year as an integer")
    @Secured({OPS_ADMIN, GLA_ORG_ADMIN, GLA_SPM, GLA_PM, GLA_FINANCE, GLA_READ_ONLY, ORG_ADMIN, PROJECT_EDITOR, PROJECT_READER,
            TECH_ADMIN})
    public Integer getCurrentAcademicYear() {
        return academicCalendar.currentYear();
    }

}
