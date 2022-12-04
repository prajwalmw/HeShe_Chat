package com.circle.chat.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.circle.chat.model.CategoryModel;
import com.circle.chat.model.User;
import com.circle.chat.model.CategoryModel;
import com.circle.chat.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SessionManager {
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;
    private int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "Circle";
    private static final String CATEGORY_SELECTED = "CATEGORY_SELECTED";
    private static final String USER_MODEL = "USER_MODEL";
    private static final String LOGGED_IN_USERNAME = "LOGGED_IN_USERNAME";
    public static final String ACCEPTED = "ACCEPTED";

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public boolean isAccepted() {
        return pref.getBoolean(ACCEPTED, false);
    }

    public void setAccepted(Boolean accepted) {
        editor.putBoolean(ACCEPTED, accepted);
        editor.commit();
    }


    public String getLoggedInUsername() {
        return pref.getString(LOGGED_IN_USERNAME, "");
    }

    public void setLoggedInUsername(String username) {
        editor.putString(LOGGED_IN_USERNAME, username);
        editor.commit();
    }

    public String getCategorySelected() {
        return pref.getString(CATEGORY_SELECTED, "");
    }

    public void setCategorySelected(String categorySelected) {
        editor.putString(CATEGORY_SELECTED, categorySelected);
        editor.commit();
    }

    public User getUserModel(String key) {
        Gson gson = new Gson();
        String json = pref.getString(key, null);
        Type type = new TypeToken<User>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void setUserModel(User usermodel, String key) {
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(usermodel);
        editor.putString(key, json);
        editor.apply();
    }


    public void saveArrayList(List<CategoryModel> list, String key){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();

    }

    public List<CategoryModel> getArrayList(String key){
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        Gson gson = new Gson();
        String json = pref.getString(key, null);
        Type type = new TypeToken<List<CategoryModel>>() {}.getType();
        return gson.fromJson(json, type);
    }

}
