package com.poiesis.login.domain.service;

public class EmailAlreadyRegisteredException extends IllegalArgumentException {
    public EmailAlreadyRegisteredException() { super("E-mail já cadastrado no sistema."); }
}
