import dev.jonium.uni.cn1.part2.Simulator;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public final class TestSupport {

    static List<Params> provider() {
        return List.of(
                new Params(40, 100, 20),
                new Params(60, 120, 20),
                new Params(100, 400, 100)
        );
    }

    static void paramTest(Params p) {
        Assertions.assertDoesNotThrow(() -> Simulator.simulate(p.minDevices, p.maxDevices, p.step));
    }

    record Params(
            int minDevices,
            int maxDevices,
            int step
    ) {
    }

}
