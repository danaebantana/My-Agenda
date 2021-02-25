package com.unipi.danaeb.myagenda;

import java.io.Serializable;

public class User implements Serializable {

    public User(String name, String address, String profession, String phoneNumber, String email){
        setName(name);
        setAddress(address);
        setProfession(profession);
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    public User(){}

    private String name;
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    private String address;
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    private String profession;
    public String getProfession() { return profession; }
    public void setProfession(String profession) { this.profession = profession; }

    private String phoneNumber;
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    private String image;
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    private String key;
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
}
