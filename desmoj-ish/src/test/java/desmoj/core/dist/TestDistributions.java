package desmoj.core.dist;

import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Temporary Class for testing purposes, will be deleted later on
 *
 * @author 8wueppen
 */
public class TestDistributions {

    private ContDistCustom     customDist;
    private Function           customFunction;
    private ContDistTriangular dist1;
    private ContDistNormal     dist2;
    private ContDistAggregate  distCombined;
    private Operator           productOp;

    @Test
    public void normalUse() {

        for (int i = 0; i < 10; i++) {

            System.out.println(customDist.sample());
        }
    }

    @BeforeEach
    public void startup() {
        Experiment experiment = new Experiment("testcase");
        Model model = new Model(null, null, false, false) {

            @Override
            public String description() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void doInitialSchedules() {
                // TODO Auto-generated method stub

            }

            @Override
            public void init() {
                // TODO Auto-generated method stub

            }
        };
        model.connectToExperiment(experiment);

        dist1 = new ContDistTriangular(model, "TriDist", 2, 5, 4, true, false);
        dist2 = new ContDistNormal(model, "NormalDist", 3, 1, true, false);

        productOp = new Operator() {

            @Override
            public String getDescription() {
                return "Product";
            }

            @Override
            public double result(double operand1, double operand2) {

                return operand1 * operand2;
            }
        };

        distCombined = new ContDistAggregate(model, "AggregateDist", dist1, dist2, productOp, true, false);

        // distCombined.sample()...

        customFunction = new Function() {

            @Override
            public String getDescription() {

                return "quadratic";
            }

            @Override
            public double value(double x) {

                return x * x;
            }
        };
        customDist = new ContDistCustom(model, "bla", customFunction, 0, 1, true, false);

    }
}
