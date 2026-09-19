package org.example.service;

import org.example.dto.ProductoRequest;
import org.example.entity.Producto;
import org.example.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public Producto crearProducto(ProductoRequest request) {
        Producto producto = new Producto(request.getNombre(), request.getPrecio(), request.getStock());
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Transactional
    public Producto actualizarStock(Long id, Integer cantidad) {
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setStock(producto.getStock() + cantidad);
        return productoRepository.save(producto);
    }
}
