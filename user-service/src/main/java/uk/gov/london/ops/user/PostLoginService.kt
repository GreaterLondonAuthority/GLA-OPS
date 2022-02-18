/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.london.ops.framework.feature.Feature
import uk.gov.london.ops.framework.feature.FeatureStatus
import uk.gov.london.ops.organisation.OrganisationType
import uk.gov.london.ops.user.UserUtils.currentUser
import java.util.*

@Service
class PostLoginService @Autowired constructor (val featureStatus: FeatureStatus)  {

    fun notifyDeprecatedOrgType(): String? {
        var text: String? = null
        if (featureStatus.isEnabled(Feature.PostLoginLegalStatusNotification)) {
            val currentUser = currentUser()
            val orgNames: MutableList<String> = ArrayList()
            for (org in currentUser.orgs) {
                if (!org.isInternalOrganisation &&
                        (org.entityType == null || OrganisationType.fromId(org.entityType).isDeprecated)) {
                    orgNames.add(org.name)
                }
            }
            text = if (orgNames.isNotEmpty()) populatePostLoginNotificationText(currentUser, orgNames) else null
        }
        return text
    }

    private fun populatePostLoginNotificationText(currentUser: User, orgNames: List<String>): String {
        return if (currentUser.isOrgAdmin) {
            "The Organisation type of " + java.lang.String.join(", ", orgNames) + " needs to be updated on the Organisation page."
        } else {
            "The Organisation type of " + java.lang.String.join(", ", orgNames) + " needs to be updated on the Organisation page by your Organisation Administrator."
        }
    }
}
