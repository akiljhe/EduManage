package com.edumanage.models;

public interface Gradable {

    double hitungRataRata();

    String getPredikat();

    default boolean isLulus() {
        return hitungRataRata() >= 75.0;
    }
}
