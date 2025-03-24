package com.toanyone.item.domain.exception;

public class ItemException extends CustomException{
    public ItemException(String message) {
        super(message);
    }

    public static class ItemNotFoundException extends ItemException {
        public ItemNotFoundException(String message) {
            super(message);
        }
    }

    public static class ItemAlreadyDeletedException extends ItemException {
        public ItemAlreadyDeletedException(String message) {
            super(message);
        }
    }

    public static class DeniedException extends ItemException {
        public DeniedException(String message) {
            super(message);
        }
    }

    public static class StoreNotFoundException extends ItemException {
        public StoreNotFoundException(String message) {
            super(message);
        }
    }

    public static class StoreServerErrorException extends ItemException {
        public StoreServerErrorException(String message) {
            super(message);
        }
    }

    public static class StockBadRequestException extends ItemException {
        public StockBadRequestException(String message) {
            super(message);
        }
    }

    public static class StockReduceException extends ItemException {
        public StockReduceException(String message) {
            super(message);
        }
    }

    public static class StockAddException extends ItemException {
        public StockAddException(String message) {
            super(message);
        }
    }

    public static class StockZeroException extends ItemException {
        public StockZeroException(String message) {
            super(message);
        }
    }
}
