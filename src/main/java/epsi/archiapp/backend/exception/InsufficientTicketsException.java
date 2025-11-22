package epsi.archiapp.backend.exception;

public class InsufficientTicketsException extends RuntimeException {
    private final int available;
    private final int requested;

    public InsufficientTicketsException(int available, int requested) {
        super(String.format("Pas assez de billets disponibles. Disponibles: %d, Demandés: %d", available, requested));
        this.available = available;
        this.requested = requested;
    }

    public int getAvailable() {
        return available;
    }

    public int getRequested() {
        return requested;
    }
}

