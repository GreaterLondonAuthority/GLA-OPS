package uk.gov.london.ops.user.implementation

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import uk.gov.london.ops.user.UserService
import javax.transaction.Transactional

@Transactional
@Service
class UserScheduledService @Autowired constructor(
    val userService: UserService,
    @Value("\${months.to.deacative.expired.users}")
    val deactivationPeriodInMonths : Int) {

    @Scheduled(cron = "\${deactivate.expired.users.cron.expression}")
    fun deactivateExpiredUsers() {
        val expiredUsers = userService.findAllUsernamesWithPasswordExpiryExceedingMonths(deactivationPeriodInMonths)

        for (expiredUser in expiredUsers) {
            userService.updateUserStatus(expiredUser, false, true)
        }
    }

}
