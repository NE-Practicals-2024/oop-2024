package rw.bnr.banking;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import rw.bnr.banking.v1.enums.ERole;
import rw.bnr.banking.v1.models.Role;
import rw.bnr.banking.v1.repositories.IRoleRepository;

@SpringBootApplication
public class BankingApplication {

    private final IRoleRepository roleRepository;

    @Autowired
    public BankingApplication(IRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(BankingApplication.class, args);
    }

    @Bean
    public boolean registerRoles() {
        Set<ERole> roles = new HashSet<>();
        roles.add(ERole.ADMIN);
        roles.add(ERole.CUSTOMER);

        for (ERole role : roles) {
            Optional<Role> roleByName = roleRepository.findByName(role);
            if (roleByName.isEmpty()) {
                Role newRole = new Role(role, role.toString());
                roleRepository.save(newRole);
                System.out.println("Created: " + role);

            }
        }
        return true;
    }
}
