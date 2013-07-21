package com.fingy.citydata.model;

public class AircraftInfo {

    private final String makeAndModel;
    private final String category;
    private final String numberOfEngines;
    private final String numberOfSeats;
    private final String weight;
    private final String speed;
    private final String engineManufacturerAndModel;
    private final String reciprocatingPower;
    private final String turboFanPower;
    private final String typeOfEngine;

    public AircraftInfo(String makeAndModel, String category, String numberOfEngines, String numberOfSeats, String weight, String speed,
            String engineManufacturerAndModel, String reciprocatingPower, String turboFanPower, String typeOfEngine) {
        this.makeAndModel = makeAndModel;
        this.category = category;
        this.numberOfEngines = numberOfEngines;
        this.numberOfSeats = numberOfSeats;
        this.weight = weight;
        this.speed = speed;
        this.engineManufacturerAndModel = engineManufacturerAndModel;
        this.reciprocatingPower = reciprocatingPower;
        this.turboFanPower = turboFanPower;
        this.typeOfEngine = typeOfEngine;
    }

    public AircraftInfo() {
        this("", "", "", "", "", "", "", "", "", "");
    }

    public String getMakeAndModel() {
        return makeAndModel;
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

    public String getEngineManufacturerAndModel() {
        return engineManufacturerAndModel;
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
        result = prime * result + ((engineManufacturerAndModel == null) ? 0 : engineManufacturerAndModel.hashCode());
        result = prime * result + ((makeAndModel == null) ? 0 : makeAndModel.hashCode());
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
        if (engineManufacturerAndModel == null) {
            if (other.engineManufacturerAndModel != null)
                return false;
        } else if (!engineManufacturerAndModel.equals(other.engineManufacturerAndModel))
            return false;
        if (makeAndModel == null) {
            if (other.makeAndModel != null)
                return false;
        } else if (!makeAndModel.equals(other.makeAndModel))
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
        builder.append(makeAndModel);
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
        builder.append(engineManufacturerAndModel);
        builder.append("§");
        builder.append(reciprocatingPower);
        builder.append("§");
        builder.append(turboFanPower);
        builder.append("§");
        builder.append(typeOfEngine);
        return builder.toString();
    }

}
