package esprit.User.dto;

/**
 * Médecins acceptés pour l’annuaire public (sans email / téléphone).
 */
public class DoctorAnnuaireDto {

    private Long id;
    private String fullName;
    private String specialty;
    private Integer yearsOfExperience;
    private Double rating;
    /** Hôpital / ville affichée sur la carte. */
    private String hospital;

    public DoctorAnnuaireDto() {
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

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }
}
