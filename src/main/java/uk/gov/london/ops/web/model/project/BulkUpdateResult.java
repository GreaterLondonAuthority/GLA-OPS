/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.web.model.project;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 10/03/2017.
 */
public class BulkUpdateResult {

    public enum Result {SUCCESS, FAILURE}

    private int failureCount;

    private int successCount;

    private Map<Integer, Result> results = new HashMap<>();

    public Integer getFailureCount() {
        return failureCount;
    }


    public Integer getSuccessCount() {
        return successCount;
    }


    public Map<Integer, Result> getResults() {
        return results;
    }

    public void recordResult(Integer projectId, Result result) {
        if (Result.SUCCESS.equals(result)) {
            successCount++;
        } else {
            failureCount++;
        }
        results.put(projectId, result);
    }
}