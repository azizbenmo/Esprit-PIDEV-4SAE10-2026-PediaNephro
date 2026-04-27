package esprit.User.dto;

/**
 * Données publiques pour la page d’accueil (sans téléphone, email ni CV).
 */
public class DoctorShowcaseDto {

    private Long id;
    private String fullName;
    private String specialty;
    private Integer yearsOfExperience;
    private Double rating;
    private String tagline;

    public DoctorShowcaseDto() {
    }

    public DoctorShowcaseDto(Long id, String fullName, String specialty, Integer yearsOfExperience,
                             Double rating, String tagline) {
        this.id = id;
        this.fullName = fullName;
        this.specialty = specialty;
        this.yearsOfExperience = yearsOfExperience;
        this.rating = rating;
        this.tagline = tagline;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public Integer getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(Integer yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }
}
