package epsi.archiapp.backend.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String resource, Long id) {
        super(String.format("Vous n'êtes pas autorisé à accéder à %s avec l'ID : %d", resource, id));
    }
}

