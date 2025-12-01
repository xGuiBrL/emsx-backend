# Ejemplos de Endpoints para Postman

## Base URL
```
http://localhost:9090/api
```

## Autenticación
Todos los endpoints requieren autenticación JWT (excepto `/api/auth/login` y `/api/auth/register`).
Incluir en el header:
```
Authorization: Bearer <token>
```

---

## 1. Crear Cliente

**POST** `/customers`

```json
{
  "name": "Juan Pérez",
  "email": "juan.perez@example.com",
  "phone": "+1234567890",
  "address": "Calle Principal 123, Ciudad"
}
```

---

## 2. Crear Producto

**POST** `/products`

```json
{
  "name": "Laptop Dell XPS 15",
  "description": "Laptop de alta gama con pantalla 4K",
  "price": 1299.99,
  "category": "Electronics",
  "sku": "DELL-XPS15-001",
  "initialStock": 50
}
```

---

## 3. Crear Orden con Ítems

**POST** `/orders`

```json
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

**Nota:** Este endpoint automáticamente:
- Calcula el total de la orden
- Reduce el stock de los productos
- Cambia el estado a CONFIRMED

---

## 4. Registrar Shipment

**POST** `/shipments`

```json
{
  "orderId": 1,
  "carrier": "FedEx",
  "estimatedDeliveryDate": "2024-12-25"
}
```

**Nota:** Se genera automáticamente un código de tracking único.

---

## 5. Consultar Historial de Órdenes por Cliente

**GET** `/customers/{id}/orders`

Ejemplo: `GET /customers/1/orders`

---

## 6. Consultar Stock de un Producto

**GET** `/products/{id}/stock`

Ejemplo: `GET /products/1/stock`

Respuesta:
```json
{
  "success": true,
  "message": "Stock retrieved successfully",
  "data": {
    "id": 1,
    "quantity": 48,
    "reservedQuantity": 0,
    "availableQuantity": 48,
    "product": {
      "id": 1,
      "name": "Laptop Dell XPS 15",
      "sku": "DELL-XPS15-001"
    }
  }
}
```

---

## 7. Seguimiento de Envío

**GET** `/shipments/track/order/{orderId}`

Ejemplo: `GET /shipments/track/order/1`

O por código de tracking:

**GET** `/shipments/track/code/{trackingCode}`

Ejemplo: `GET /shipments/track/code/TRK-A1B2C3D4`

---

## Flujo Completo (Pasos 1 a 6)

### Paso 1: Crear Cliente
```bash
POST /customers
{
  "name": "María García",
  "email": "maria.garcia@example.com",
  "phone": "+9876543210",
  "address": "Avenida Central 456"
}
```

### Paso 2: Crear Productos
```bash
POST /products
{
  "name": "Mouse Logitech MX Master 3",
  "description": "Mouse inalámbrico ergonómico",
  "price": 99.99,
  "category": "Accessories",
  "sku": "LOG-MX3-001",
  "initialStock": 100
}

POST /products
{
  "name": "Teclado Mecánico RGB",
  "description": "Teclado mecánico con iluminación RGB",
  "price": 149.99,
  "category": "Accessories",
  "sku": "KEY-MECH-001",
  "initialStock": 75
}
```

### Paso 3: Crear Orden
```bash
POST /orders
{
  "customerId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 3
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

### Paso 4: Verificar Stock Reducido
```bash
GET /products/1/stock
GET /products/2/stock
```

### Paso 5: Crear Shipment
```bash
POST /shipments
{
  "orderId": 1,
  "carrier": "UPS",
  "estimatedDeliveryDate": "2024-12-30"
}
```

### Paso 6: Consultar Historial del Cliente
```bash
GET /customers/1/orders
```

### Paso 7: Seguimiento del Envío
```bash
GET /shipments/track/order/1
```

---

## Otros Endpoints Útiles

### Listar Todos los Clientes
**GET** `/customers`

### Listar Todos los Productos
**GET** `/products`

### Listar Productos Disponibles (con stock > 0)
**GET** `/products/available`

### Listar Productos por Categoría
**GET** `/products/category/{category}`

Ejemplo: `GET /products/category/Electronics`

### Listar Todas las Órdenes
**GET** `/orders`

### Obtener Orden por ID
**GET** `/orders/{id}`

### Actualizar Estado de Orden
**PUT** `/orders/{id}/status?status=SHIPPED`

Estados válidos: `PENDING`, `CONFIRMED`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`

### Listar Todos los Shipments
**GET** `/shipments`

### Actualizar Estado de Shipment
**PUT** `/shipments/{id}/status?status=IN_TRANSIT`

Estados válidos: `PENDING`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `RETURNED`

---

## Notas Importantes

1. **Reducción Automática de Stock**: Cuando se crea una orden, el stock se reduce automáticamente.
2. **Cálculo Automático de Total**: El total de la orden se calcula automáticamente sumando los subtotales de cada ítem.
3. **Tracking Code Único**: Cada shipment recibe un código de tracking único generado automáticamente (formato: `TRK-XXXXXXXX`).
4. **Validaciones**: 
   - No se puede crear una orden si no hay stock suficiente
   - No se puede crear un shipment si la orden ya tiene uno
   - El email del cliente debe ser único

