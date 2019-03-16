package ca.appsimulations.models.examples.common;

import ca.appsimulations.jlqninterface.lqn.entities.ActivityDefBase;
import ca.appsimulations.jlqninterface.lqn.entities.ActivityPhases;
import ca.appsimulations.jlqninterface.lqn.model.LqnModel;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ResponseTime {

    public static double getResponseTime(LqnModel lqnModelResult, String entryName) {
        String activityName = lqnModelResult.entryByName(entryName).getEntryPhaseActivities().getActivityAtPhase(1)
                .getName();
        List<ActivityDefBase> activities =
                lqnModelResult.activities().stream().filter(activityDefBase -> activityDefBase.getName().equals
                        (activityName)).collect(
                        toList());

        double responseTime = 0;
        if (activities.size() > 0) {
            ActivityPhases ap = (ActivityPhases) activities.get(0);
            responseTime = ap.getResult().getService_time();
        }
        return responseTime;
    }
}
