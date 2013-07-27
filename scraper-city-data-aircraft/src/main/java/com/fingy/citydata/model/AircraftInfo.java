package com.fingy.citydata.model;

public class AircraftInfo {

    private final String make;
    private final String model;
    private final String category;
    private final String numberOfEngines;
    private final String numberOfSeats;
    private final String weight;
    private final String speed;
    private final String engineMake;
    private final String engineModel;
    private final String reciprocatingPower;
    private final String turboFanPower;
    private final String typeOfEngine;

    public AircraftInfo(String make, String model, String category, String numberOfEngines, String numberOfSeats, String weight,
            String speed, String engineMake, String engineModel, String reciprocatingPower, String turboFanPower, String typeOfEngine) {
        this.make = make;
        this.model = model;
        this.category = category;
        this.numberOfEngines = numberOfEngines;
        this.numberOfSeats = numberOfSeats;
        this.weight = weight;
        this.speed = speed;
        this.engineMake = engineMake;
        this.engineModel = engineModel;
        this.reciprocatingPower = reciprocatingPower;
        this.turboFanPower = turboFanPower;
        this.typeOfEngine = typeOfEngine;
    }

    public AircraftInfo() {
        this("", "", "", "", "", "", "", "", "", "", "", "");
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getCategory() {
        return category;
    }

    public String getNumberOfEngines() {
        return numberOfEngines;
    }

    public String getNumberOfSeats() {
        return numberOfSeats;
    }

    public String getWeight() {
        return weight;
    }

    public String getSpeed() {
        return speed;
    }

    public String getEngineMake() {
        return engineMake;
    }

    public String getEngineModel() {
        return engineModel;
    }

    public String getReciprocatingPower() {
        return reciprocatingPower;
    }

    public String getTurboFanPower() {
        return turboFanPower;
    }

    public String getTypeOfEngine() {
        return typeOfEngine;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((category == null) ? 0 : category.hashCode());
        result = prime * result + ((engineMake == null) ? 0 : engineMake.hashCode());
        result = prime * result + ((engineModel == null) ? 0 : engineModel.hashCode());
        result = prime * result + ((make == null) ? 0 : make.hashCode());
        result = prime * result + ((model == null) ? 0 : model.hashCode());
        result = prime * result + ((numberOfEngines == null) ? 0 : numberOfEngines.hashCode());
        result = prime * result + ((numberOfSeats == null) ? 0 : numberOfSeats.hashCode());
        result = prime * result + ((reciprocatingPower == null) ? 0 : reciprocatingPower.hashCode());
        result = prime * result + ((speed == null) ? 0 : speed.hashCode());
        result = prime * result + ((turboFanPower == null) ? 0 : turboFanPower.hashCode());
        result = prime * result + ((typeOfEngine == null) ? 0 : typeOfEngine.hashCode());
        result = prime * result + ((weight == null) ? 0 : weight.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AircraftInfo other = (AircraftInfo) obj;
        if (category == null) {
            if (other.category != null)
                return false;
        } else if (!category.equals(other.category))
            return false;
        if (engineMake == null) {
            if (other.engineMake != null)
                return false;
        } else if (!engineMake.equals(other.engineMake))
            return false;
        if (engineModel == null) {
            if (other.engineModel != null)
                return false;
        } else if (!engineModel.equals(other.engineModel))
            return false;
        if (make == null) {
            if (other.make != null)
                return false;
        } else if (!make.equals(other.make))
            return false;
        if (model == null) {
            if (other.model != null)
                return false;
        } else if (!model.equals(other.model))
            return false;
        if (numberOfEngines == null) {
            if (other.numberOfEngines != null)
                return false;
        } else if (!numberOfEngines.equals(other.numberOfEngines))
            return false;
        if (numberOfSeats == null) {
            if (other.numberOfSeats != null)
                return false;
        } else if (!numberOfSeats.equals(other.numberOfSeats))
            return false;
        if (reciprocatingPower == null) {
            if (other.reciprocatingPower != null)
                return false;
        } else if (!reciprocatingPower.equals(other.reciprocatingPower))
            return false;
        if (speed == null) {
            if (other.speed != null)
                return false;
        } else if (!speed.equals(other.speed))
            return false;
        if (turboFanPower == null) {
            if (other.turboFanPower != null)
                return false;
        } else if (!turboFanPower.equals(other.turboFanPower))
            return false;
        if (typeOfEngine == null) {
            if (other.typeOfEngine != null)
                return false;
        } else if (!typeOfEngine.equals(other.typeOfEngine))
            return false;
        if (weight == null) {
            if (other.weight != null)
                return false;
        } else if (!weight.equals(other.weight))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(make);
        builder.append("§");
        builder.append(model);
        builder.append("§");
        builder.append(category);
        builder.append("§");
        builder.append(numberOfEngines);
        builder.append("§");
        builder.append(numberOfSeats);
        builder.append("§");
        builder.append(weight);
        builder.append("§");
        builder.append(speed);
        builder.append("§");
        builder.append(engineMake);
        builder.append("§");
        builder.append(engineModel);
        builder.append("§");
        builder.append(reciprocatingPower);
        builder.append("§");
        builder.append(turboFanPower);
        builder.append("§");
        builder.append(typeOfEngine);
        return builder.toString();
    }

}
