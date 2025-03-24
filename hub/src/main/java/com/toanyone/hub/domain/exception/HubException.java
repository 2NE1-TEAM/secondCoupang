package com.toanyone.hub.domain.exception;


public class HubException extends CustomException {
    public HubException(String message) {
        super(message);
    }

    public static class HubAlreadyDeletedException extends HubException {
        public HubAlreadyDeletedException(String message) {
            super(message);
        }
    }

    public static class HubDuplicateException extends HubException {
        public HubDuplicateException(String message) {
            super(message);
        }
    }

    public static class HubNotFoundException extends HubException {
        public HubNotFoundException(String message) {
            super(message);
        }
    }

    public static class HubDeniedException extends HubException {
        public HubDeniedException(String message) {
            super(message);
        }
    }

    public static class SlackServerErrorException extends HubException {
        public SlackServerErrorException(String message) {
            super(message);
        }
    }
}
