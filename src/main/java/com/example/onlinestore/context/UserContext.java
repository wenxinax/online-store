package com.example.onlinestore.context;


import com.example.onlinestore.bean.Member;

public class UserContext {
    private static final ThreadLocal<Member> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(Member user) {
        currentUser.set(user);
    }

    public static Member getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
} 