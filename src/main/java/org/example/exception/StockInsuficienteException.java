package org.example.exception;

public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String productoNombre, int stockActual, int cantidadPedida) {
        super(String.format(
            "Stock insuficiente para '%s'. Stock disponible: %d, cantidad pedida: %d.",
            productoNombre, stockActual, cantidadPedida
        ));
    }
}
