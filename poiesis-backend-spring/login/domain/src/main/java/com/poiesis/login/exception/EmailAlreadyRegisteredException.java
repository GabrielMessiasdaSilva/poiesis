package com.poiesis.login.exception;

public class EmailAlreadyRegisteredException extends IllegalArgumentException {
    public EmailAlreadyRegisteredException() { super("E-mail já cadastrado no sistema."); }
}
