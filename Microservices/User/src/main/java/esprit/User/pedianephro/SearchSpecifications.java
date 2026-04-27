package esprit.User.pedianephro;

import esprit.User.entities.Doctor;
import esprit.User.entities.Parent;
import esprit.User.entities.Patient;
import esprit.User.entities.User;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SearchSpecifications {

    public static Specification<User> userSearchSpec(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("username")), like));
            predicates.add(cb.like(cb.lower(root.get("email")), like));
            Long id = parseLong(q);
            if (id != null) {
                predicates.add(cb.equal(root.get("id"), id));
            }
            return cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Parent> parentSearchSpec(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("fullName")), like));
            var userJoin = root.join("user");
            predicates.add(cb.like(cb.lower(userJoin.get("username")), like));
            predicates.add(cb.like(cb.lower(userJoin.get("email")), like));
            Long id = parseLong(q);
            if (id != null) {
                predicates.add(cb.equal(userJoin.get("id"), id));
            }
            return cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Patient> patientSearchSpec(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("fullName")), like));
            var userJoin = root.join("user");
            predicates.add(cb.like(cb.lower(userJoin.get("username")), like));
            predicates.add(cb.like(cb.lower(userJoin.get("email")), like));
            Long id = parseLong(q);
            if (id != null) {
                predicates.add(cb.equal(userJoin.get("id"), id));
            }
            return cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    public static Specification<Doctor> doctorSearchSpec(String q) {
        return (root, query, cb) -> {
            if (q == null || q.trim().isEmpty()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.like(cb.lower(root.get("fullName")), like));
            var userJoin = root.join("user");
            predicates.add(cb.like(cb.lower(userJoin.get("username")), like));
            predicates.add(cb.like(cb.lower(userJoin.get("email")), like));
            Long id = parseLong(q);
            if (id != null) {
                predicates.add(cb.equal(userJoin.get("id"), id));
            }
            return cb.or(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private static Long parseLong(String s) {
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }
}


