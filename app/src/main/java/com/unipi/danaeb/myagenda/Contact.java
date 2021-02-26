package com.unipi.danaeb.myagenda;

public class Contact{

    private String name;
    private String phoneNumber;
    private boolean selected;
    private String uid;

    public Contact(String n, String pn){
        setName(n);
        setPhoneNumber(pn);
        setSelected(false);
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }

    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public boolean isSelected() { return selected; }

    public void setSelected(boolean selected) { this.selected = selected; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }
}
