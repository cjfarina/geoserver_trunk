package org.geoserver.bxml.filter_1_1.spatial;

public class Distance {

    private double value;

    private String units;

    public Distance(double value, String units) {
        super();
        this.value = value;
        this.units = units;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

}
