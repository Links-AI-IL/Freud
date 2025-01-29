package com.links.freud;

public class User {

    public String name;
    public String email;
    public String password;
    public String phone;
    public int age;
    public String phone1sos;
    public String phone2sos;
    public String phone3sos;
    public String gender;
    public  String avatar;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String password, String phone, int age, String phone1sos, String phone2sos, String phone3sos, String gender) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.phone = phone;
        this.phone1sos = phone1sos;
        this.phone2sos = phone2sos;
        this.phone3sos = phone3sos;
        this.gender = gender;
    }

    public User(String name, String email, String password, String phone, int age, String phone1sos, String phone2sos, String phone3sos, String gender, String avatar) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.age = age;
        this.phone1sos = phone1sos;
        this.phone2sos = phone2sos;
        this.phone3sos = phone3sos;
        this.gender = gender;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getPhone1sos() {
        return phone1sos;
    }

    public void setPhone1sos(String phone1sos) {
        this.phone1sos = phone1sos;
    }

    public String getPhone2sos() {
        return phone2sos;
    }

    public void setPhone2sos(String phone2sos) {
        this.phone2sos = phone2sos;
    }

    public String getPhone3sos() {
        return phone3sos;
    }

    public void setPhone3sos(String phone3sos) {
        this.phone3sos = phone3sos;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
