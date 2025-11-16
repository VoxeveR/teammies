package com.voxever.teammies.auth.exception;

public class CredentialsIsAlreadyTakenException extends RuntimeException {
  public CredentialsIsAlreadyTakenException(String message) {
    super(message);
  }
}
