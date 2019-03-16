package ca.appsimulations.models.examples.common;

import ca.appsimulations.jlqninterface.lqn.model.LqnXmlDetails;
import ca.appsimulations.jlqninterface.lqn.model.SolverParams;

public class SolverCommonParams {
    //default values
    private static final double CONVERGENCE = 0.01;
    private static final int ITERATION_LIMIT = 50_000;
    private static final double UNDER_RELAX_COEFF = 0.9;
    private static final int PRINT_INTERVAL = 1;
    private static final String XML_NS_URL = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String SCHEMA_LOCATION = "lqn.xsd";
    private static final String XML_NAME = "input-rep";
    private static final String XML_DESCRIPTION = "description";
    private static final String COMMENT = "comment";

    public static LqnXmlDetails buildLqnXmlDetails(){
        return SolverCommonParams.buildLqnXmlDetails(XML_NAME, XML_NS_URL, COMMENT, XML_DESCRIPTION, SCHEMA_LOCATION);
    }

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

    public static SolverParams buildSolverParams() {
        return SolverCommonParams.buildSolverParams(COMMENT, CONVERGENCE, ITERATION_LIMIT, UNDER_RELAX_COEFF,
                                                     PRINT_INTERVAL);
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
