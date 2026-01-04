package com.voxever.teammies.auth.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long userId) {
        super("User not found!");
        System.out.println("User not found with id:" + userId);
    }
}