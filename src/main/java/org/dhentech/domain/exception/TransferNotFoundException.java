package org.dhentech.domain.exception;

public class TransferNotFoundException extends RuntimeException {

    public TransferNotFoundException(Long id) {
        super("Transfer not found with id: " + id);
    }
}
