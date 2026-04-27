package esprit.User.controller;

import esprit.User.dto.AdminRegistrationDto;
import esprit.User.dto.AdminUpdateDto;
import esprit.User.entities.Admin;
import esprit.User.services.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"mic1/admins", "admin"})
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/register")
    public Admin createAdmin(@RequestBody AdminRegistrationDto dto) {
        return adminService.createAdminWithUser(dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Admin updateAdmin(@PathVariable Long id, @RequestBody AdminUpdateDto dto) {
        return adminService.updateAdminWithUser(id, dto);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Admin> getAllAdmins() {
        return adminService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Admin getAdmin(@PathVariable Long id) {
        return adminService.findById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAdmin(@PathVariable Long id) {
        adminService.delete(id);
    }
}
