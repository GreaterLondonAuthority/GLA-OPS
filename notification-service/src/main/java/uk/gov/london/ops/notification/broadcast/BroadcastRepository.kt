package uk.gov.london.ops.notification.broadcast

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BroadcastRepository: JpaRepository<BroadcastEntity, Int> {
    fun findAllByEmailSentAndStatus( sent: Boolean, status: BroadcastStatus) : Set<BroadcastEntity>
}

