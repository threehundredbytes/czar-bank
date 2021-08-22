package ru.dreadblade.czarbank.exception;

public class NotEnoughBalanceException extends BaseException {
    public NotEnoughBalanceException() {
        super(ExceptionMessage.NOT_ENOUGH_BALANCE.getMessage(), ExceptionMessage.NOT_ENOUGH_BALANCE.getStatus());
    }
}
