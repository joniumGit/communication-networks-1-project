import dev.jonium.uni.cn1.part1.Part1;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeSharedOverSubscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static dev.jonium.uni.cn1.part1.Part1.*;

class Q2Test {

    private static final long TIMEOUT = 1;

    @Test
    void mips250vm2() {
        Assertions.assertTimeoutPreemptively(
                Duration.of(TIMEOUT, ChronoUnit.SECONDS),
                () -> new Part1(new SimulationSettings(
                        List.of(new DatacenterSettings(List.of(new HostSettings()))),
                        VMSettings.identical(
                                2,
                                "Xen",
                                250,
                                new CloudletSchedulerTimeShared(),
                                List.of(new CloudletSettings(100_000))
                        )
                )).simulate());
    }

    @Test
    void mips500vm2() {
        Assertions.assertTimeoutPreemptively(
                Duration.of(TIMEOUT, ChronoUnit.SECONDS),
                () -> new Part1(new SimulationSettings(
                        List.of(new DatacenterSettings(List.of(new HostSettings()))),
                        VMSettings.identical(
                                2,
                                "Xen",
                                500,
                                new CloudletSchedulerTimeShared(),
                                List.of(new CloudletSettings(200_000))
                        )
                )).simulate());
    }

    @Test
    void mips250vm3() {
        Assertions.assertTimeoutPreemptively(
                Duration.of(TIMEOUT, ChronoUnit.SECONDS),
                () -> new Part1(new SimulationSettings(
                        List.of(new DatacenterSettings(List.of(new HostSettings()))),
                        VMSettings.identical(
                                3,
                                "Xen",
                                250,
                                new CloudletSchedulerTimeShared(),
                                List.of(new CloudletSettings(100_000))
                        )
                )).simulate());
    }

    @Test
    void mips500vm3() {
        Assertions.assertTimeoutPreemptively(
                Duration.of(TIMEOUT, ChronoUnit.SECONDS),
                () -> new Part1(new SimulationSettings(
                        List.of(new DatacenterSettings(List.of(new HostSettings(new VmSchedulerTimeSharedOverSubscription())))),
                        VMSettings.identical(
                                3,
                                "Xen",
                                500,
                                new CloudletSchedulerTimeShared(),
                                List.of(new CloudletSettings(200_000))
                        )
                )).simulate());
    }

}
