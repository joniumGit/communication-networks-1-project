package dev.jonium.uni.cn1.part2;

import com.mechalikh.pureedgesim.simulationcore.SimLog;
import com.mechalikh.pureedgesim.simulationcore.Simulation;

import java.io.IOError;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public final class Simulator {

    private Simulator() {
    }

    private static void delete(Path p) {
        try {
            Files.delete(p);
        } catch (IOException e) {
            throw new IOError(e);
        }
    }

    public static void simulate(
            int minDevices,
            int maxDevices,
            int step
    ) throws IOException {
        var td = Files.createTempDirectory("cn1-tmp-");
        var paths = new ArrayList<Path>();

        try {
            var jarLocation = Arrays.stream(System.getProperty("java.class.path").split(System.getProperty("path.separator")))
                    .filter(s -> s.toLowerCase().contains("pureedgesim"))
                    .findFirst()
                    .orElseThrow(IOException::new);

            try (var fs = FileSystems.newFileSystem(Paths.get(jarLocation), Map.of("create", "false"))) {
                try (var pathStream = Files.walk(fs.getPath("settings"), 1)) {
                    pathStream.forEach(p -> {
                        try {
                            var fp = td.resolve(p.getFileName().toString());
                            paths.add(fp);
                            Files.copy(p, fp);
                        } catch (IOException e) {
                            throw new IOError(e);
                        }
                    });
                }
            }
        } finally {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                paths.forEach(Simulator::delete);
                delete(td);
            }));
        }

        simulate(
                td,
                minDevices,
                maxDevices,
                step
        );
    }

    private static void simulate(String settingsLocation) {
        var sim = new Simulation();
        sim.setCustomSettingsFolder(settingsLocation);
        sim.launchSimulation();
    }

    private static void simulate(
            Path settingsLocation,
            int minDevices,
            int maxDevices,
            int step
    ) throws IOException {
        var settings = settingsLocation.resolve("simulation_parameters.properties");
        Properties props;
        try (var io = Files.newInputStream(settings)) {
            props = new Properties();
            props.load(io);
            props.setProperty("parallel_simulation", "false");
            props.setProperty("min_number_of_edge_devices", String.valueOf(minDevices));
            props.setProperty("max_number_of_edge_devices", String.valueOf(maxDevices));
            props.setProperty("edge_device_counter_size", String.valueOf(step));


            props.setProperty("auto_close_real_time_charts", "true");
            props.setProperty("display_real_time_charts", "true");
            props.setProperty("orchestration_algorithms", "ROUND_ROBIN");
            props.setProperty("pause_length", "3");
            props.setProperty("update_interval", "0.1");
        }
        try (var os = Files.newOutputStream(settings, StandardOpenOption.TRUNCATE_EXISTING)) {
            props.store(os, null);
        }
        SimLog.println("Simulation devices: " + minDevices + "-" + maxDevices + "-" + step);
        simulate(settingsLocation + FileSystems.getDefault().getSeparator());
    }

}
