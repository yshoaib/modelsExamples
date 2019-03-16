package ca.appsimulations.models.examples.common;

import ca.appsimulations.jlqninterface.lqn.model.LqnXmlDetails;
import ca.appsimulations.jlqninterface.lqn.model.SolverParams;

public class SolverCommonParams {
    public static LqnXmlDetails buildLqnXmlDetails(String name,
                                                   String xmlnsXsi,
                                                   String comment,
                                                   String description,
                                                   String schemaLocation) {


        return LqnXmlDetails
                .builder()
                .name(name)
                .xmlnsXsi(xmlnsXsi)
                .comment(comment)
                .description(description)
                .schemaLocation(schemaLocation)
                .build();
    }

    public static SolverParams buildSolverParams(String comment,
                                                  double convergence,
                                                  int iterationLimit,
                                                  double underRelaxCoeff,
                                                  int printInterval) {


        return SolverParams
                .builder()
                .comment(comment)
                .convergence(convergence)
                .iterationLimit(iterationLimit)
                .underRelaxCoeff(underRelaxCoeff)
                .printInterval(printInterval)
                .build();
    }
}
