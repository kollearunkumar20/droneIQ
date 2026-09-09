package com.droneiq.config;

import com.droneiq.auth.entity.Role;
import com.droneiq.auth.entity.User;
import com.droneiq.auth.repository.UserRepository;
import com.droneiq.drone.entity.Drone;
import com.droneiq.drone.entity.DroneStatus;
import com.droneiq.drone.repository.DroneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final DroneRepository droneRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            DroneRepository droneRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.droneRepository = droneRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedDrones();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@droneiq.io")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default admin user: admin / admin123");
        }

        if (!userRepository.existsByUsername("operator")) {
            User operator = User.builder()
                    .username("operator")
                    .email("operator@droneiq.io")
                    .passwordHash(passwordEncoder.encode("operator123"))
                    .role(Role.OPERATOR)
                    .build();
            userRepository.save(operator);
            log.info("Initialized default operator user: operator / operator123");
        }
    }

    private void seedDrones() {
        if (droneRepository.count() == 0) {
            List<Drone> initialFleet = List.of(
                    Drone.builder()
                            .droneId("DRONE-001")
                            .model("Quadcopter Alpha")
                            .status(DroneStatus.OFFLINE)
                            .firmwareVersion("v2.4.1")
                            .ipAddress("192.168.1.101")
                            .build(),
                    Drone.builder()
                            .droneId("DRONE-002")
                            .model("Hexacopter Beta")
                            .status(DroneStatus.OFFLINE)
                            .firmwareVersion("v3.1.0")
                            .ipAddress("192.168.1.102")
                            .build(),
                    Drone.builder()
                            .droneId("DRONE-003")
                            .model("VTOL Explorer")
                            .status(DroneStatus.OFFLINE)
                            .firmwareVersion("v1.8.4")
                            .ipAddress("192.168.1.103")
                            .build()
            );
            droneRepository.saveAll(initialFleet);
            log.info("Initialized default drone fleet: 3 drones registered");
        }
    }
}
