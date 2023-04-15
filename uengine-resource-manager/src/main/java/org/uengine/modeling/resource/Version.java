package org.uengine.modeling.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by jjy on 2016. 1. 15..
 */
public class Version implements Serializable{

    int major;
    int minor;
    String description;
    Calendar date;
    private boolean production;

    public Version(){}

    public Version(String version) {

        try {
            String[] majorAndMinor = version.split("\\.");
            setMajor(Integer.parseInt(majorAndMinor[0]));
            setMinor(Integer.parseInt(majorAndMinor[1]));
        }catch (Exception e){
            throw new RuntimeException("Unable to parse the version: " + version, e);
        }
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }



    public void setProduction(boolean production) {
        this.production = production;
    }

    public boolean isProduction() {
        return production;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj == null || !(obj instanceof Version)) return false;

        Version versionObj = (Version)obj;

        return (getMajor() == versionObj.getMajor() && getMinor() == versionObj.getMinor());
    }

    @Override
    public String toString() {
        return major + "." + minor;
    }
}
