package dev.jonium.uni.cn1.part1;


import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyRoundRobin;
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
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple example showing how to create a datacenter with one host and run one
 * cloudlet on it.
 * <p>
 * Copied from
 * https://github.com/muneeb666/CNCloudSimLab/blob/4fb48907a08c2878e464dfdaaed1a12f48da0ca5/cloudsimCnLab/cloudsim-3.0.3/sources/datacenterSimulation/Task1.java
 * and modified to work with <b>cloudsimplus</b>
 */
@SuppressWarnings("SameParameterValue")
public class Part1 {

    public static void main(String[] args) {
        var q1i = new Part1(new SimulationSettings(
                400000,
                1000,
                1,
                1
        ));
        q1i.simulate();

        var q1ii = new Part1(new SimulationSettings(
                400000,
                500,
                1,
                1
        ));
        q1ii.simulate();

        var q2i = new Part1(new SimulationSettings(
                100000,
                250,
                1,
                2
        ));
        q2i.simulate();

        var q2ii = new Part1(new SimulationSettings(
                200000,
                500,
                1,
                2
        ));
        q2ii.simulate();


        // TODO: 3 VM
        var q2iii = new Part1(new SimulationSettings(
                100000,
                250,
                1,
                3
        ));
        // q2iii.simulate();

        var q2iiii = new Part1(new SimulationSettings(
                200000,
                500,
                1,
                3
        ));
        //q2iiii.simulate();
    }

    private final List<Cloudlet> cloudlets;
    private final List<Vm> vms;
    private final Logger log;
    private final CloudSim simulation;
    private final SimulationSettings settings;

    private static final long PES = 1; // number of cpus

    public Part1(SimulationSettings settings) {
        this.settings = settings;
        cloudlets = new ArrayList<>();
        vms = new ArrayList<>();
        simulation = new CloudSim();
        log = CloudSim.LOGGER;
    }

    private void simulate() {
        log.info("Starting CloudSimExample1...\n");

        // Make default Datacenter
        createDatacenter("DEFAULT");

        // Broker and other entities
        var broker = createBroker("DEFAULT");

        for (int j = 0; j < settings.hostVmCount; j++) {
            var vm = createVM("Xen " + j);
            var cl = createCloudlet();
            broker.bindCloudletToVm(cl, vm);
            cloudlets.add(cl);
            vms.add(vm);
        }

        // Submit
        broker.submitVmList(vms);
        broker.submitCloudletList(cloudlets);

        // Run
        simulation.start();
        printCloudletList();
        log.info("CloudSimExample1 finished!");
    }

    private Cloudlet createCloudlet() {
        // Cloudlet properties
        long length = settings.cloudletLength;
        long fileSize = 300;
        long outputSize = 300;
        var utilizationModel = new UtilizationModelFull();

        var cloudlet = new CloudletSimple(length, PES);
        cloudlet.setFileSize(fileSize);
        cloudlet.setOutputSize(outputSize);
        cloudlet.setUtilizationModel(utilizationModel); // Same model for all

        return cloudlet;
    }

    private Vm createVM(String name) {
        // VM description
        var mips = settings.mips;
        var size = 10000; // image size (MB)
        var ram = 512; // vm memory (MB)
        var bw = 1000;


        // create VM
        var vm = new VmSimple(mips, PES);
        vm.setCloudletScheduler(settings.hostCount > 1
                ? new CloudletSchedulerSpaceShared()
                : new CloudletSchedulerTimeShared()
        );
        vm.setBw(bw);
        vm.setSize(size);
        vm.setRam(ram);
        vm.setDescription(name);

        return vm;
    }


    private void createDatacenter(String name) {
        var hosts = new ArrayList<Host>();
        var pes = new ArrayList<Pe>();

        int mips = 1000;
        pes.add(new PeSimple(mips));

        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        for (int i = 0; i < settings.hostCount; i++) {
            var hs = new HostSimple(
                    ram,
                    bw,
                    storage,
                    pes
            );
            hs.setVmScheduler(new VmSchedulerTimeShared());
            hosts.add(hs);
        }

        var arch = "x86"; // system architecture
        var os = "Linux"; // operating system
        var vmm = "Xen";
        var timeZone = 10.0; // time zone this resource located
        var cost = 3.0; // the cost of using processing in this resource
        var costPerMem = 0.05; // the cost of using memory in this resource
        var costPerStorage = 0.001; // the cost of using storage in this
        var costPerBw = 0.0; // the cost of using bw in this resource

        var dc = new DatacenterSimple(
                simulation,
                hosts,
                new VmAllocationPolicySimple(),
                Collections.emptyList()
        );
        dc.setTimeZone(timeZone);
        dc.setName(name);

        var charac = dc.getCharacteristics();
        charac.setArchitecture(arch);
        charac.setOs(os);
        charac.setVmm(vmm);
        charac.setCostPerBw(costPerBw);
        charac.setCostPerMem(costPerMem);
        charac.setCostPerStorage(costPerStorage);
        charac.setCostPerSecond(cost);
    }

    private DatacenterBroker createBroker(String name) {
        return new DatacenterBrokerSimple(simulation, name);
    }

    private void printCloudletList() {
        var indent = "    ";
        log.info("\n========== OUTPUT ==========\n");
        log.info(
                "Cloudlet ID{}End Status{}    Data Center{}Virtual Machine{}    Time{}Start Time{}Finish Time",
                indent, indent, indent, indent, indent, indent
        );

        var dft = new DecimalFormat("###.##");
        for (Cloudlet cloudlet : cloudlets) {
            var sb = new StringBuilder();
            sb.append(String.format("%1$11d", cloudlet.getId()))
                    .append(indent)
                    .append(String.format("%1$10s", cloudlet.getStatus()))
                    .append(indent)
                    .append(String.format("%1$15s", cloudlet.getLastTriedDatacenter().getName()))
                    .append(indent)
                    .append(String.format("%1$15s", cloudlet.getVm().getDescription()))
                    .append(indent)
                    .append(String.format("%1$8s", dft.format(cloudlet.getActualCpuTime())))
                    .append(indent)
                    .append(String.format("%1$10s", dft.format(cloudlet.getExecStartTime())))
                    .append(indent)
                    .append(String.format("%1$11s", dft.format(cloudlet.getFinishTime())));
            log.info("{}", sb);
        }
    }

    public record SimulationSettings(
            long cloudletLength,
            long mips,
            int hostCount,
            int hostVmCount
    ) {
    }
}