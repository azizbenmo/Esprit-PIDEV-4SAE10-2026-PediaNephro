package esprit.User.controller;

import esprit.User.entities.Parent;
import esprit.User.dto.ParentRegistrationDto;
import esprit.User.dto.ParentUpdateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import esprit.User.services.ParentService;

import java.util.List;

@RestController
@RequestMapping("mic1/parents")
public class ParentController {


    @Autowired
    private ParentService parentService;

    // Créer un parent + user
    @PostMapping("/register")
    public Parent createParent(@RequestBody ParentRegistrationDto dto) {
        return parentService.createParentWithUser(dto);
    }

    // Modifier un parent + user
    @PutMapping("/{id}")
    public Parent updateParent(@PathVariable Long id, @RequestBody ParentUpdateDto dto) {
        return parentService.updateParentWithUser(id, dto);
    }

    // Liste des parents
    @GetMapping
    public List<Parent> getAllParents() {
        return parentService.findAll();
    }

    // Détail d'un parent
    @GetMapping("/{id}")
    public Parent getParent(@PathVariable Long id) {
        return parentService.findById(id);
    }

    // Suppression d'un parent + user
    @DeleteMapping("/{id}")
    public void deleteParent(@PathVariable Long id) {
        parentService.delete(id);
    }
}
