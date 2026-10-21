package com.poiesis.login.exception;

public class SessaoInvalidaException extends RuntimeException {
    public SessaoInvalidaException() { super("Sessão inválida."); }
}
