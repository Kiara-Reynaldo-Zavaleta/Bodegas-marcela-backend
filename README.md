# Bodegas Marcela - Backend

API REST para un sistema de inventario y ventas pensado para una bodega (tienda de barrio). Permite registrar productos, controlar el stock, generar boletas de venta, manejar ventas al crédito (fiado) y ver reportes básicos del negocio, como qué día se vende más o cuáles son los productos más vendidos.

## Tecnologías

Java con Spring Boot y Maven. La base de datos es PostgreSQL, hospedada en Neon. El servicio corre en Render usando Docker.

## Qué hace

- CRUD de productos, con control de stock y opción de desactivar productos sin borrarlos
- Registro de boletas de venta, con forma de pago (efectivo, Yape o Plin) y opción de venta al crédito
- Historial de compras por cliente, buscando por DNI
- Reportes: día más productivo, productos más vendidos y ventas por hora
- Marcar boletas fiadas como pagadas

## Cómo correrlo localmente

Necesitas tener Java 21 y PostgreSQL instalados. Crea una base de datos local y configura las siguientes variables de entorno antes de levantar el proyecto:

spring.datasource.url=jdbc:postgresql://localhost:5432/bodegas_marcela
SPRING_DATASOURCE_USERNAME=tu_usuario
SPRING_DATASOURCE_PASSWORD=tu_contraseña
Luego, desde la raíz del proyecto:

mvn spring-boot:run


El servidor levanta por defecto en el puerto 8080. En producción, el puerto y el resto de la configuración se leen igual desde variables de entorno.
