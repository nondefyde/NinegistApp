package zumma.com.ninegistapp.model;

import android.content.Context;

import com.parse.ParseUser;

import zumma.com.ninegistapp.ParseConstants;

/**
 * Created by Kaba Yusuf on 2/16/2015.
 */
public class BasicInfo {

    private String phone_number;
    private String country_name;
    private String country_code;
    private String status;
    private String picture;
    private Context context;


    public BasicInfo(){}

    public BasicInfo(Context context,String phone_number, String fullName, String country_name, String country_code, String picture) {
        this.context = context;
        this.phone_number = phone_number;
        this.country_name = country_name;
        this.country_code = country_code;
        this.status = "-no status-";
        this.picture = picture;
    }

    public BasicInfo(Context context,ParseUser user) {
        this.context = context;
        this.phone_number = user.getUsername();
        this.country_name = (String) user.get(ParseConstants.KEY_COUNTRY_NAME);
        this.country_code = (String) user.get(ParseConstants.KEY_COUNTRY_CODE);
        this.status = "-no status-";
        this.picture = null;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public String getCountry_name() {
        return country_name;
    }

    public String getCountry_code() {
        return country_code;
    }

    public String getPicture() {
        return picture;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public void setCountry_name(String country_name) {
        this.country_name = country_name;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }
}
