package uk.gov.london.ops.notification

data class UpdateNotificationStatusRequest(val id: Int, val status: UserNotificationStatus)

