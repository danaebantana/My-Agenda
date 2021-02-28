package com.unipi.danaeb.myagenda;

import java.util.ArrayList;

public class Event {

    private String title, time;
    private ArrayList<String> collaborators, comments, attendance;

    public Event() { }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getTime() { return time; }

    public void setTime(String time) { this.time = time; }

    public ArrayList<String> getCollaborators() { return collaborators; }

    public void setCollaborators(ArrayList<String> collaborators) { this.collaborators = collaborators; }

    public ArrayList<String> getComments() { return comments; }

    public void setComments(ArrayList<String> comments) { this.comments = comments; }

    public ArrayList<String> getAttendance() { return attendance; }

    public void setAttendance(ArrayList<String> attendance) { this.attendance = attendance; }
}
