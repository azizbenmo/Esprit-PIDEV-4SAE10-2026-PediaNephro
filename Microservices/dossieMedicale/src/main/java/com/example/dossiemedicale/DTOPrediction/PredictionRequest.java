package com.example.dossiemedicale.DTOPrediction;

import java.util.List;

public class PredictionRequest {

    private List<Double> values;


    public PredictionRequest() {
    }


    public PredictionRequest(List<Double> values) {
        this.values = values;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }
}
