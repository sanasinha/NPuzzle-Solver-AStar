package tests.pas.npuzzle;

// SYSTEM IMPORTS
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;



// JAVA PROJECT IMPORTS
import tests.pas.npuzzle.agents.NPuzzleAgentTests;
import tests.pas.npuzzle.planners.AStarPlannerTests;

public class RunAllTests {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(AStarPlannerTests.class)) 
                .selectors(selectClass(NPuzzleAgentTests.class))
                .build();

        Launcher launcher = LauncherFactory.create();

        // need this to capture results
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);

        launcher.execute(request);
        TestExecutionSummary summary = listener.getSummary();

        // print results
        System.out.println("=== TEST RESULTS ===");
        System.out.println("Tests Found: " + summary.getTestsFoundCount());
        System.out.println("Tests Succeeded: " + summary.getTestsSucceededCount());
        System.out.println("Tests Failed: " + summary.getTestsFailedCount());
        System.out.println();
        System.out.println();

        // print out location + stack trace for each failure
        System.out.println("=== TEST FAILURES ===");
        for(TestExecutionSummary.Failure f : summary.getFailures()) {
            TestIdentifier id = f.getTestIdentifier();
            System.out.println("Name=" + id.getDisplayName() + " source=" + id.getSource());
            System.out.print("Reason=");
            f.getException().printStackTrace();
        }
    }
}
