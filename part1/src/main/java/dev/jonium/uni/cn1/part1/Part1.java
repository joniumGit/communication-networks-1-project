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
        var p = new Part1();
        p.simulate();
    }

    private final List<Cloudlet> cloudlets;
    private final List<Vm> vms;
    private final Logger log;
    private final CloudSim simulation;
    private static final long PES = 1; // number of cpus

    public Part1() {
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
        var vm = createVM("Xen");
        var cl = createCloudlet(0);

        // Submit VM's
        vms.add(vm);
        broker.submitVmList(vms);

        // Submit cloudlets to default VM
        cloudlets.add(cl);
        broker.submitCloudletList(cloudlets, vm);

        // Run
        simulation.start();
        printCloudletList();
        log.info("CloudSimExample1 finished!");
    }

    private Cloudlet createCloudlet(int id) {
        // Cloudlet properties
        long length = 20000;
        long fileSize = 300;
        long outputSize = 300;
        var utilizationModel = new UtilizationModelFull();

        var cloudlet = new CloudletSimple(id, length, PES);
        cloudlet.setFileSize(fileSize);
        cloudlet.setOutputSize(outputSize);
        cloudlet.setUtilizationModel(utilizationModel); // Same model for all

        return cloudlet;
    }

    private Vm createVM(String name) {
        // VM description
        var vmid = 0;
        var mips = 800;
        var size = 10000; // image size (MB)
        var ram = 512; // vm memory (MB)
        var bw = 1000;


        // create VM
        var vm = new VmSimple(vmid, mips, PES);
        vm.setCloudletScheduler(new CloudletSchedulerTimeShared());
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

        int hostId = 0;
        int ram = 2048; // host memory (MB)
        long storage = 1000000; // host storage
        int bw = 10000;

        var hs = new HostSimple(
                ram,
                bw,
                storage,
                pes
        );
        hs.setId(hostId);
        hs.setVmScheduler(new VmSchedulerTimeShared());
        hosts.add(hs);

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

}