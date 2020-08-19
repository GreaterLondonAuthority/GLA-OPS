/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.postlogin

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ops.framework.feature.Feature
import uk.gov.london.ops.framework.feature.FeatureStatus
import uk.gov.london.ops.user.UserService
import uk.gov.london.ops.user.domain.User
import java.util.*

@Service
class PostLoginService @Autowired constructor (val featureStatus: FeatureStatus,
                                               val userService: UserService)  {

    fun notifyMissingLegalStatus(): String? {
        var text: String? = null
        if (featureStatus.isEnabled(Feature.PostLoginLegalStatusNotification)) {
            val currentUser = userService.currentUser()
            val orgNames: MutableList<String> = ArrayList()
            for (org in currentUser.organisations) {
                if (!org.isInternalOrganisation && (org.legalStatus == null || org.legalStatus.getName().isEmpty())) {
                    orgNames.add(org.name)
                }
            }
            text = if (orgNames.isNotEmpty()) populateLegalStatusText(currentUser, orgNames) else null
        }
        return text
    }

    private fun populateLegalStatusText(currentUser: User, orgNames: List<String>): String {
        return if (currentUser.isOrgAdmin) {
            "The legal status of " + java.lang.String.join(", ", orgNames) + " needs to be updated on the Organisation page. " +
                    "Note you can also add a 'Finance contact email' to receive payment summary email updates for each payment we make relating to your project(s)."
        } else {
            "The legal status of " + java.lang.String.join(", ", orgNames) + " needs to be updated on the Organisation page by your Organisation Administrator. " +
                    "Note they can also add a 'Finance contact email' to receive payment summary email updates for each payment we make relating to your project(s)."
        }
    }
}