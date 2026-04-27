package esprit.User.controller;

import esprit.User.entities.User;
import esprit.User.dto.PaginatedResponse;
import esprit.User.pedianephro.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import esprit.User.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/mic1/utilisateurs")
public class UserController {


    @Autowired
    private UserService UserService;

    @PostMapping("/ajouterUtilisateur")
    public User ajouterUtilisateur(@RequestBody User u) {
        return UserService.ajouterUser(u);
    }

    @PutMapping("/modifierUtilisateur")
    public User modifierUtilisateur(@RequestBody User u) {
        return UserService.modifierUser(u);
    }

    @DeleteMapping("/supprimerUtilisateur/{id}")
    public void supprimerUtilisateur(@PathVariable Long id) {
        UserService.supprimerUser(id);
    }

    @GetMapping("/listeUtilisateurs")
    public List<User> listeUtilisateurs() {
        return UserService.listeUsers();
    }

    @GetMapping
    public PaginatedResponse<User> searchUsers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    ) {
        Pageable pageable = PaginationUtils.buildPageable(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> result = UserService.search(q, pageable);
        return PaginationUtils.fromPage(result);
    }
    @GetMapping("/findByUsername/{username}")
    public User findByUsername(@PathVariable String username) {
        return UserService.findByUsername(username);
    }
}

