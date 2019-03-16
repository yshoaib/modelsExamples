package ca.appsimulations.models.examples.ucc2012;


import ca.appsimulations.jlqninterface.lqn.model.LqnModel;
import ca.appsimulations.jlqninterface.lqn.model.LqnXmlDetails;
import ca.appsimulations.jlqninterface.lqn.model.SolverParams;
import ca.appsimulations.jlqninterface.lqn.model.handler.LqnSolver;
import ca.appsimulations.jlqninterface.lqn.model.parser.LqnResultParser;
import ca.appsimulations.jlqninterface.lqn.model.writer.LqnModelWriter;
import ca.appsimulations.models.model.application.App;
import ca.appsimulations.models.model.application.AppBuilder;
import ca.appsimulations.models.model.cloud.Cloud;
import ca.appsimulations.models.model.cloud.CloudBuilder;
import ca.appsimulations.models.model.lqnmodel.LqnModelFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

import static ca.appsimulations.models.examples.common.ResponseTime.getResponseTime;
import static ca.appsimulations.models.examples.common.SolverCommonParams.buildLqnXmlDetails;
import static ca.appsimulations.models.examples.common.SolverCommonParams.buildSolverParams;
import static ca.appsimulations.models.model.cloud.ContainerType.*;
import static java.util.Arrays.asList;

// Model used in "Using Layered Bottlenecks for Virtual Machine Provisioning in the Clouds"
@Slf4j
public class ucc2012 {

    public static void main(String[] args) throws Exception {

        File inputFile = new File("input.lqnx");
        LqnXmlDetails xmlDetails = buildLqnXmlDetails();
        SolverParams solverParams = buildSolverParams();

        File intermediateInputFile = new File("intermediateInputFile.lqnx");
        File outputFile = new File("output.lqxo");
        File outputPs = new File("output.ps");
        outputFile.delete();
        outputPs.delete();

        int users = 100;
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

    }

    private static App buildApp(String appName, int users, int maxReplicas, double responseTimeObjective) {
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

    private static Cloud buildCloud(App app) {
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
}
