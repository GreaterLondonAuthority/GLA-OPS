/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowCallbackHandler
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import uk.gov.london.ops.framework.exception.NotFoundException
import uk.gov.london.ops.framework.feature.Feature
import uk.gov.london.ops.framework.feature.FeatureStatus
import uk.gov.london.ops.user.UserUtils.currentUser
import java.io.File
import java.util.*

/**
 * Service providing data for the user dashboard.
 */
@Service
class DashboardService @Autowired constructor(val jdbc: JdbcTemplate,
                                              val featureStatus: FeatureStatus,
                                              @Value("\${gc.log.file}") val gcLogFile: String) {

    /**
     * Return the dashboard metrics for the currently logged-in user.
     */
    fun getMetricsForCurrentUser(): Map<String, Int> {
        val currentUser = currentUser() ?: throw AccessDeniedException("session not found")

        if (!featureStatus.isEnabled(Feature.Dashboard)) {
            throw NotFoundException()
        }

        val username = currentUser.username

        val metrics = TreeMap<String, Int>()

        val rowCallbackHandler = RowCallbackHandler { rs -> metrics[rs.getString("key")] = rs.getInt("value") }

        jdbc.query("SELECT key, value FROM v_dashboard_metrics WHERE username = ?", rowCallbackHandler, username)

        return metrics
    }

    /**
     * Returns the summary of key data entity counts to be displayed on dash board page.
     */
    fun getSummaryOfKeyDataEntityCounts(): Map<String, String> {
        val keyDataEntityCounts = TreeMap<String, String>()

        val rowCallbackHandler = RowCallbackHandler { rs -> keyDataEntityCounts.put(rs.getString("key"), rs.getString("value")) }

        jdbc.query("SELECT key, value FROM v_dashboard_key_data_entity_counts", rowCallbackHandler)

        return keyDataEntityCounts
    }

    val gcLogRegex = "\\d+K".toRegex()

    fun getGCData(): GCData {
        val series = arrayOf("Mem size before GC", "Mem size after GC", "Total available mmm")
        var labels = emptyArray<String>()
        val data: List<MutableList<Int>> = listOf(mutableListOf(), mutableListOf(), mutableListOf())

        var counter = 0
        val file = File(gcLogFile)
        if (file.exists()) {
            file.forEachLine { line ->
                gcLogRegex.findAll(line).forEachIndexed { index, res ->
                    data[index].add(res.value.replace("K", "").toInt())
                }
                labels += counter++.toString()
            }
        }

        return GCData(series, labels, data)
    }

}

class GCData(val series: Array<String>,
             val labels: Array<String>,
             val data: List<List<Int>>)
