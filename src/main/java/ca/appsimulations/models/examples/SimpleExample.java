package ca.appsimulations.models.examples;

import ca.appsimulations.jlqninterface.lqn.entities.ActivityDefBase;
import ca.appsimulations.jlqninterface.lqn.entities.ActivityPhases;
import ca.appsimulations.jlqninterface.lqn.model.LqnModel;
import ca.appsimulations.jlqninterface.lqn.model.LqnXmlDetails;
import ca.appsimulations.jlqninterface.lqn.model.SolverParams;
import ca.appsimulations.jlqninterface.lqn.model.handler.LqnSolver;
import ca.appsimulations.jlqninterface.lqn.model.parser.LqnInputParser;
import ca.appsimulations.jlqninterface.lqn.model.parser.LqnResultParser;
import ca.appsimulations.jlqninterface.lqn.model.writer.LqnModelWriter;
import ca.appsimulations.models.model.application.App;
import ca.appsimulations.models.model.application.AppBuilder;
import ca.appsimulations.models.model.cloud.Cloud;
import ca.appsimulations.models.model.cloud.CloudBuilder;
import ca.appsimulations.models.model.lqnmodel.LqnModelFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;

import static ca.appsimulations.models.examples.common.ResponseTime.getResponseTime;
import static ca.appsimulations.models.model.cloud.ContainerType.*;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Slf4j
public class SimpleExample {
    private static final double CONVERGENCE = 0.01;
    private static final int ITERATION_LIMIT = 50_000;
    private static final double UNDER_RELAX_COEFF = 0.9;
    private static final int PRINT_INTERVAL = 1;
    private static final String XML_NS_URL = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String SCHEMA_LOCATION = "lqn.xsd";
    private static final String XML_NAME = "input-rep";
    private static final String XML_DESCRIPTION = "description";
    private static final String COMMENT = "comment";

    public static void main(String[] args) throws Exception {

        File inputFile = new File("input.lqnx");
        LqnXmlDetails xmlDetails = buildLqnXmlDetails();
        SolverParams solverParams = buildSolverParams();

        File intermediateInputFile = new File("intermediateInputFile.lqnx");
        File outputFile = new File("output.lqxo");
        File outputPs = new File("output.ps");
        outputFile.delete();
        outputPs.delete();

        int i =1;
        int users = 1;
        do{
            App testApp = buildApp("testApp", users, 5, 50000.0);
            Cloud testCloud = buildCloud(testApp);

            LqnModel lqnModel = LqnModelFactory.build(testApp, xmlDetails, solverParams);
            new LqnModelWriter().write(lqnModel, inputFile.getAbsolutePath());

            log.info("Users: " + lqnModel.entryByName("load_1").getTask().getMutiplicityString());

            //write intermediate input
            new LqnModelWriter().write(lqnModel, intermediateInputFile.getAbsolutePath());


            //solve
            boolean solveResult =
                    LqnSolver.solveLqns(intermediateInputFile.getAbsolutePath(),
                                        new LqnResultParser(new LqnModel()),
                                        outputFile.getAbsolutePath());

            //was the model solved successfully
            if (solveResult == false) {
                log.error("problem solving lqn model");
                return;
            }

            //find response time
            LqnModel lqnModelResult = new LqnModel();
            new LqnResultParser(lqnModelResult).parseFile(outputFile.getAbsolutePath());
            log.info("response time of load_1 is: " + getResponseTime(lqnModelResult, "load_1"));

            LqnSolver.savePostScript(outputFile.getAbsolutePath(),
                                     outputPs.getAbsolutePath());

            users = i * 10; //users = 1, 10, 20, ..., 100
            i++;

        }while(i <= 11);
    }

    private static App buildApp(String appName, int users, int maxReplicas, double responseTimeObjective){
        App app = AppBuilder.builder()
                .name(appName)
                .maxReplicas(maxReplicas)
                .responseTimeObjective(responseTimeObjective)
                .service("Browser", users)
                .serviceEntry("load", "load_1", 2.0, 7.0)
                .buildService()
                .service("TaskA", 10)
                .serviceEntry("funcA1", "funcA1_1", 3.0)
                .buildService()
                .service("TaskB", 10)
                .serviceEntry("funcB1", "funcB1_1", 2.0)
                .buildService()
                .service("TaskC", 10)
                .serviceEntry("funcC1", "funcC1_1", 5.0)
                .buildService()
                .service("TaskD", 10)
                .serviceEntry("funcD1", "funcD1_1", 3.0)
                .buildService()
                .call("call1", "Browser", "TaskA", "load", "funcA1", 1)
                .call("call2", "TaskA", "TaskB", "funcA1", "funcB1", 3)
                .call("call3", "TaskB", "TaskC", "funcB1", "funcC1", 1)
                .call("call4", "TaskC", "TaskD", "funcC1", "funcD1", 2)
                .build();

        return app;
    }

    private static Cloud buildCloud(App app){
        Cloud cloud = CloudBuilder.builder()
                .name("cloud1")
                .containerTypes(asList(SM, MD, LA))
                .containerImage("Browser")
                .service("Browser", app)
                .buildContainerImage()
                .containerImage("TaskA")
                .service("TaskA", app)
                .buildContainerImage()
                .containerImage("TaskB")
                .service("TaskB", app)
                .buildContainerImage()
                .containerImage("TaskC")
                .service("TaskC", app)
                .buildContainerImage()
                .containerImage("TaskD")
                .service("TaskD", app)
                .buildContainerImage()
                .build();

        cloud.instantiateContainer("pClient", "Browser", SM);
        cloud.instantiateContainer("pTaskA", "TaskA", SM);
        cloud.instantiateContainer("pTaskB", "TaskB", SM);
        cloud.instantiateContainer("pTaskC", "TaskC", SM);
        cloud.instantiateContainer("pTaskD", "TaskD", SM);
        return cloud;
    }

    private static SolverParams buildSolverParams() {
        return SolverParams
                .builder()
                .comment(COMMENT)
                .convergence(CONVERGENCE)
                .iterationLimit(ITERATION_LIMIT)
                .underRelaxCoeff(UNDER_RELAX_COEFF)
                .printInterval(PRINT_INTERVAL)
                .build();
    }

    private static LqnXmlDetails buildLqnXmlDetails() {
        return LqnXmlDetails
                .builder()
                .name(XML_NAME)
                .xmlnsXsi(XML_NS_URL)
                .description(XML_DESCRIPTION)
                .schemaLocation(SCHEMA_LOCATION)
                .build();
    }
}
