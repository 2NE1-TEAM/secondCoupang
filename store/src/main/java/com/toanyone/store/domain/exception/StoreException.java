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

    public static class StoreDuplicateException extends StoreException {
        public StoreDuplicateException(String message) {
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

    public static class HubServerErrorException extends StoreException {
        public HubServerErrorException(String message) {
            super(message);
        }
    }

    public static class StoreDeniedException extends StoreException {
        public StoreDeniedException(String message) {
            super(message);
        }
    }
}
