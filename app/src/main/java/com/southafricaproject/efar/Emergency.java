package com.southafricaproject.efar;

/**
 * Created by mackfitzpatrick on 6/1/18.
 */

class Emergency {
    private String key;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone_number;
    private String info;
    private String creationDate;
    private String respondingEfar;
    private String state;

    // constructor
    public Emergency(String key, String address, Double latitude, Double longitude,
                     String phone_number, String info, String creationDate, String respondingEfar, String state) {
        this.key = key;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.phone_number = phone_number;
        this.info = info;
        this.creationDate = creationDate;
        this.respondingEfar = respondingEfar;
        this.state = state;
    }

    // getter
    public String getKey() { return key; }
    public String getAddress() { return address; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public String getPhone() { return phone_number; }
    public String getInfo() { return info; }
    public String getCreationDate() { return creationDate; }
    public String getRespondingEfar() { return respondingEfar; }
    public String getState() { return state; }
}
