import dev.jonium.uni.cn1.part1.Part1;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static dev.jonium.uni.cn1.part1.Part1.*;

class Q1Test {

    private static final long TIMEOUT = 1;

    @Test
    void mips1k() {
        Assertions.assertTimeoutPreemptively(
                Duration.of(TIMEOUT, ChronoUnit.SECONDS),
                () -> new Part1(new SimulationSettings(
                        List.of(new DatacenterSettings(
                                List.of(new HostSettings())
                        )),
                        List.of(new VMSettings(
                                "Xen 1",
                                1000,
                                new CloudletSchedulerTimeShared(),
                                List.of(new CloudletSettings(400_000))
                        ))
                )).simulate());
    }

    @Test
    void mips500() {
        Assertions.assertTimeoutPreemptively(
                Duration.of(TIMEOUT, ChronoUnit.SECONDS),
                () -> new Part1(new SimulationSettings(
                        List.of(new DatacenterSettings(
                                List.of(new HostSettings())
                        )),
                        List.of(new VMSettings(
                                "Xen 1",
                                500,
                                new CloudletSchedulerTimeShared(),
                                List.of(new CloudletSettings(400_000))
                        ))
                )).simulate());
    }

}
