package esprit.User.dto;

import java.util.List;

public class FaceRegisterRequest {
    private List<Double> descriptor;

    public List<Double> getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(List<Double> descriptor) {
        this.descriptor = descriptor;
    }
}
