package com.toanyone.store.domain.exception;

public class StoreException extends CustomException{
    public StoreException(String message) {
        super(message);
    }

    public static class HubNotFoundException extends StoreException {
        public HubNotFoundException(String message) {
            super(message);
        }
    }

    public static class StoreNameExistException extends StoreException {
        public StoreNameExistException(String message) {
            super(message);
        }
    }

    public static class StoreNotFoundException extends StoreException {
        public StoreNotFoundException(String message) {
            super(message);
        }
    }

    public static class StoreAlreadyDeletedException extends StoreException {
        public StoreAlreadyDeletedException(String message) {
            super(message);
        }
    }
}
