package com.links.freud;

public class loginUser {

    public String email;

    public String password;

    public loginUser() {

    }

    public loginUser( String email, String password) {

        email = email;
        password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        password = password;
    }
}
