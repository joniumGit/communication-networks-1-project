package dev.jonium.uni.cn1.part1;


import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmScheduler;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple example showing how to create a datacenter with one host and run one
 * cloudlet on it.
 * <p>
 * Tries to emulate the Task1 from
 * <a href="https://github.com/muneeb666/CNCloudSimLab/blob/4fb48907a08c2878e464dfdaaed1a12f48da0ca5/cloudsimCnLab/cloudsim-3.0.3/sources/datacenterSimulation/Task1.java">
 * GitHub Link</a>
 * and modified to work with <b>cloudsimplus</b>
 */
@SuppressWarnings("SameParameterValue")
public class Part1 {

    private final Logger log;
    private final CloudSim simulation;
    private final SimulationSettings settings;

    public Part1(SimulationSettings settings) {
        this.settings = settings;
        simulation = new CloudSim();
        log = CloudSim.LOGGER;
    }

    public void simulate() {
        log.info("Starting CloudSimExample1...\n");

        // Make default Datacenter
        settings.datacenters.forEach(this::createDatacenter);

        // Broker and other entities
        var broker = createBroker("broker");

        for (var vmSettings : settings.vms) {
            var vm = createVM(vmSettings);
            broker.submitVm(vm);
            for (var clSettings : vmSettings.cloudlets) {
                var cl = createCloudlet(clSettings);
                broker.submitCloudlet(cl);
                broker.bindCloudletToVm(cl, vm);
            }
        }

        // Run
        simulation.start();

        new CloudletsTableBuilder(broker.getCloudletSubmittedList()).build();
        log.info("CloudSimExample1 finished!");
    }

    private Cloudlet createCloudlet(CloudletSettings setings) {

        var cloudlet = new CloudletSimple(setings.length, setings.pes);
        cloudlet.setFileSize(setings.fileSize);
        cloudlet.setOutputSize(setings.outputSize);
        cloudlet.setUtilizationModel(setings.utilizationModel); // Same model for all

        return cloudlet;
    }

    private Vm createVM(VMSettings settings) {

        var vm = new VmSimple(settings.mips, settings.pes);
        vm.setCloudletScheduler(settings.scheduler);
        vm.setBw(settings.bw);
        vm.setSize(settings.imageSize);
        vm.setRam(settings.ram);
        vm.setDescription(settings.name);

        return vm;
    }


    private void createDatacenter(DatacenterSettings settings) {
        var hosts = new ArrayList<Host>();

        for (var hostSetting : settings.hosts) {
            var pes = new ArrayList<Pe>();
            for (long i = 0; i < hostSetting.nPE; i++) {
                pes.add(new PeSimple(hostSetting.mipsPerPE));
            }
            var hs = new HostSimple(
                    hostSetting.ram,
                    hostSetting.bw,
                    hostSetting.storage,
                    pes
            );
            hs.setVmScheduler(hostSetting.scheduler);
            hosts.add(hs);
        }

        var dc = new DatacenterSimple(
                simulation,
                hosts,
                new VmAllocationPolicySimple(),
                Collections.emptyList()
        );
        dc.setTimeZone(settings.timeZone);
        dc.setName(settings.vmm);

        var charac = dc.getCharacteristics();
        charac.setArchitecture(settings.arch);
        charac.setOs(settings.os);
        charac.setVmm(settings.vmm);
        charac.setCostPerBw(settings.costPerBandWidth);
        charac.setCostPerMem(settings.costPerMemory);
        charac.setCostPerStorage(settings.costPerStorage);
        charac.setCostPerSecond(settings.costPerSecond);
    }

    private DatacenterBroker createBroker(String name) {
        return new DatacenterBrokerSimple(simulation, name);
    }

    public record HostSettings(
            long ram,
            long bw,
            long storage,
            long mipsPerPE,
            long nPE,
            VmScheduler scheduler
    ) {
        public HostSettings() {
            this(null, null, null);
        }

        public HostSettings(VmScheduler scheduler) {
            this(null, null, scheduler);
        }

        public HostSettings(Long nPE, Long mipsPerPE, VmScheduler scheduler) {
            this(
                    2048,
                    10000,
                    1_000_000,
                    mipsPerPE == null ? 1000 : mipsPerPE,
                    nPE == null ? 1 : nPE,
                    scheduler == null ? new VmSchedulerTimeShared() : scheduler
            );
        }
    }

    public record VMSettings(
            String name,
            long mips,
            long pes,
            long ram,
            long bw,
            long imageSize,
            CloudletScheduler scheduler,
            List<CloudletSettings> cloudlets
    ) {
        public VMSettings(
                String name,
                long mips,
                CloudletScheduler scheduler,
                List<CloudletSettings> cloudlets
        ) {
            this(
                    name,
                    mips,
                    1,
                    512,
                    1000,
                    10_000,
                    scheduler,
                    cloudlets
            );
        }

        public static List<VMSettings> identical(
                long count,
                String name,
                long mips,
                CloudletScheduler scheduler,
                List<CloudletSettings> cloudlets
        ) {
            var out = new ArrayList<VMSettings>();
            for (long i = 0; i < count; i++) {
                try {
                    out.add(new VMSettings(
                            String.format("%s %d", name, i),
                            mips,
                            scheduler.getClass().getConstructor().newInstance(),
                            cloudlets
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return out;
        }
    }

    public record DatacenterSettings(
            String vmm,
            String arch,
            String os,
            double timeZone,
            double costPerSecond,
            double costPerMemory,
            double costPerStorage,
            double costPerBandWidth,
            List<HostSettings> hosts
    ) {
        public DatacenterSettings(List<HostSettings> hosts) {
            this(
                    "Xen",
                    "x86",
                    "Linux",
                    10.0,
                    3.0,
                    0.05,
                    0.001,
                    0.0,
                    hosts
            );
        }

        @SuppressWarnings("unused")
        public DatacenterSettings(String vmm, List<HostSettings> hosts) {
            this(
                    vmm,
                    "x86",
                    "Linux",
                    10.0,
                    3.0,
                    0.05,
                    0.001,
                    0.0,
                    hosts
            );
        }
    }

    public record CloudletSettings(
            long pes,
            long length,
            long fileSize,
            long outputSize,
            UtilizationModel utilizationModel
    ) {
        public CloudletSettings(long length) {
            this(
                    1,
                    length,
                    300,
                    300,
                    new UtilizationModelFull()
            );
        }
    }

    public record SimulationSettings(
            List<DatacenterSettings> datacenters,
            List<VMSettings> vms
    ) {
    }


}