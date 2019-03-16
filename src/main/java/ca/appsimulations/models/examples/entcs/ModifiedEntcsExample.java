package ca.appsimulations.models.examples.entcs;

import ca.appsimulations.jlqninterface.lqn.model.LqnModel;
import ca.appsimulations.jlqninterface.lqn.model.LqnXmlDetails;
import ca.appsimulations.jlqninterface.lqn.model.SolverParams;
import ca.appsimulations.jlqninterface.lqn.model.handler.LqnSolver;
import ca.appsimulations.jlqninterface.lqn.model.parser.LqnResultParser;
import ca.appsimulations.jlqninterface.lqn.model.writer.LqnModelWriter;
import ca.appsimulations.models.examples.common.SolverCommonParams;
import ca.appsimulations.models.model.application.*;
import ca.appsimulations.models.model.cloud.Cloud;
import ca.appsimulations.models.model.cloud.CloudBuilder;
import ca.appsimulations.models.model.cloud.ContainerType;
import ca.appsimulations.models.model.cloud.EntcsCloudBuilder;
import ca.appsimulations.models.model.lqnmodel.LqnModelFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;

import static ca.appsimulations.models.examples.common.ResponseTime.getResponseTime;

// Differences from ENTCS 2011 paper "Web application performance modeling using layered queueing networks"
//1. Threads of task DB is set to 10 instead of 150.
//2. Threads of task AppServer is set to 20 instead of 100.
//3. Network resources are not included in the model
//4. DB and AppServer are on different processors.

@Slf4j
public class ModifiedEntcsExample {
    public static void main(String[] args) throws Exception {
        File inputFile = new File("input.lqnx");
        File outputFile = new File("test-output.lqnx");
        File intermediateInputFile = new File("intermediateInputFile.lqnx");
        File outputPs = new File("output.ps");
        outputFile.delete();
        outputPs.delete();

        int users = 20;

        String appName = "test";
        int maxReplicas = 10;
        double responseTime = 350.0;
        App app = buildApp(appName,users, maxReplicas, responseTime);

        EntcsCloudBuilder.build(app);

        LqnXmlDetails xmlDetails = SolverCommonParams.buildLqnXmlDetails();
        SolverParams solverParams = SolverCommonParams.buildSolverParams();
        LqnModel lqnModel = LqnModelFactory.build(app, xmlDetails, solverParams);
        new LqnModelWriter().write(lqnModel, inputFile.getAbsolutePath());
        LqnSolver.savePostScript(inputFile.getAbsolutePath(), "entcs.ps");

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

    private static App buildApp(String appName,int users, int maxReplicas, double responseTimeObjective) {
        App app = AppBuilder.builder().name("app").maxReplicas(maxReplicas).
                responseTimeObjective(responseTimeObjective).
                service("Browser", users).
                serviceEntry("load", "load_1", 0.0001D, 7.0).buildService().
                service("AppServer", 20).
                serviceEntry("sendHTML", "sendHTML_1", 0.00962D).
                serviceEntry("sendJS1", "sendJS1_1", 0.00085D).
                serviceEntry("sendJS2", "sendJS2_1", 0.0048D).
                serviceEntry("sendJS3", "sendJS3_1", 0.00064D).
                serviceEntry("viewRoutes", "viewRoutes_1", 0.09555D).
                serviceEntry("routing1", "routing1_1", 0.21167D).
                serviceEntry("add1", "add1_1", 0.05342D).
                serviceEntry("routing2", "routing2_1", 0.18616D).
                serviceEntry("add2", "add2_1", 0.05343D).buildService().
                service("DB", 10).
                serviceEntry("dbViewRoutes", "dbViewRoutes_1", 0.02938D).
                serviceEntry("dbRouting1", "dbRouting1_1", 0.25272D).
                serviceEntry("dbAdd1", "dbAdd1_1", 0.00043D).
                serviceEntry("dbRouting2", "dbRouting2_1", 0.05209D).
                serviceEntry("dbAdd2", "dbAdd2_1", 0.00041D).buildService().
                service("Disk", 1).
                serviceEntry("disk1", "disk1_1", 0.00001D).
                serviceEntry("disk2", "disk2_1", 0.00001D).
                serviceEntry("disk3", "disk3_1", 0.00001D).
                serviceEntry("disk4", "disk4_1", 0.00002D).
                serviceEntry("disk5", "disk5_1", 0.00006D).
                serviceEntry("disk6", "disk6_1", 0.00028D).
                serviceEntry("disk7", "disk7_1", 0.00032D).
                serviceEntry("disk8", "disk8_1", 0.00023D).
                serviceEntry("disk9", "disk9_1", 0.00033D).buildService().
                call("call1", "Browser", "AppServer", "load", "sendHTML", 1).
                call("call2", "Browser", "AppServer", "load", "sendJS1", 1).
                call("call3", "Browser", "AppServer", "load", "sendJS2", 1).
                call("call4", "Browser", "AppServer", "load", "sendJS3", 1).
                call("call5", "Browser", "AppServer", "load", "viewRoutes", 1).
                call("call6", "Browser", "AppServer", "load", "routing1", 1).
                call("call7", "Browser", "AppServer", "load", "add1", 1).
                call("call8", "Browser", "AppServer", "load", "routing2", 1).
                call("call9", "Browser", "AppServer", "load", "add2", 1).
                call("call10", "AppServer", "DB", "viewRoutes", "dbViewRoutes", 1).
                call("call11", "AppServer", "DB", "routing1", "dbRouting1", 1).
                call("call12", "AppServer", "DB", "add1", "dbAdd1", 1).
                call("call13", "AppServer", "DB", "routing2", "dbRouting2", 1).
                call("call14", "AppServer", "DB", "add2", "dbAdd2", 1).
                call("call15", "AppServer", "Disk", "sendHTML", "disk1", 1).
                call("call16", "AppServer", "Disk", "sendJS1", "disk2", 1).
                call("call17", "AppServer", "Disk", "sendJS2", "disk3", 1).
                call("call18", "AppServer", "Disk", "sendJS3", "disk4", 1).
                call("call19", "DB", "Disk", "dbViewRoutes", "disk5", 1).
                call("call20", "DB", "Disk", "dbRouting1", "disk6", 1).
                call("call21", "DB", "Disk", "dbAdd1", "disk7", 1).
                call("call22", "DB", "Disk", "dbRouting2", "disk8", 1).
                call("call23", "DB", "Disk", "dbAdd2", "disk9", 1).build();
        return app;
    }
}

