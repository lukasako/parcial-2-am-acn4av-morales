# GastApp - Control de Gastos Personales

GastApp es una aplicación móvil desarrollada para permitir a los usuarios administrar de manera simple y visual sus ingresos, egresos, movimientos fijos y categorías personalizadas.
Este trabajo corresponde al final de la materia Aplicaciones Móviles, incorporando ahora login con Firebase, Firestore para persistencia de datos y todas las pantallas funcionales del sistema.

---

## Funcionalidades implementadas 

- **Pantalla principal** con:
    - Saludo dinámico al usuario.
    - Saldo total formateado como moneda local.
    - Ícono de billetera dinámico (llena o vacía según el monto).
    - Tarjeta interactiva de saldo que alterna entre saldo y gráfico circular de ingresos/egresos.
- **Botones principales**: Ingresar gasto, Movimientos, Categorías, Movimientos fijos.
- **Sección de últimos movimientos**:
    - Lista dinámica con hasta 3 movimientos recientes.
    - Cada ítem incluye título, monto (coloreado según ingreso/egreso), categoría, fecha y medio de pago.
<img width="304" height="678" alt="image" src="https://github.com/user-attachments/assets/dd155476-e0ad-48c3-b69c-4803e2f168d2" />

- **Footer de navegación** con iconos para Home y Cuentas.
- **Integración completa con Firebase**
    - Autenticación
    - Registro de usuario con email y contraseña.
    - Login persistente.
    - Manejo automático de sesión.
    - Firestore Database
    - Cada usuario tiene su propia colección:
    - Cada operación CRUD impacta en Firestore en tiempo real.
- **Implementación de varias pantallas para la navegación**

## Capturas reales de la aplicación

  <img width="304" height="678" alt="ingresar-movimiento" src="https://github.com/user-attachments/assets/28ef2db5-1aa2-4bbb-be47-c460aa5c9fa4" />

  <img width="304" height="678" alt="movimientos" src="https://github.com/user-attachments/assets/8af8b412-7249-4b73-b7c8-63fc048bbc30" />

  <img width="304" height="678" alt="categorias" src="https://github.com/user-attachments/assets/eb8b0e26-5439-4251-a866-8bc819782f93" />

  <img width="304" height="678" alt="movimientos_fijjos" src="https://github.com/user-attachments/assets/7c07a343-d781-49d1-81eb-9ebdc5499085" />

  <img width="304" height="678" alt="image" src="https://github.com/user-attachments/assets/7d043330-0e10-4a16-888c-09ba48aa041a" />
  
  <img width="304" height="678" alt="cuentas" src="https://github.com/user-attachments/assets/e0e8340a-1240-485c-8bb3-468293732173" />

---

## Mockup de referencia
El diseño inicial fue realizado en **Canva**, simulando la vista final de la pantalla principal, asi como el informe

---

## Links importantes

- **Repositorio GitHub**: https://github.com/lukasako/parcial-2-am-acn4av-morales
- **Todos los archivos relevantes para el informe (google drive)**: https://drive.google.com/drive/u/2/folders/1OsCaO-_BjtSKJLOdMi-amaJm8SmkUwyy

---

## Profesor

- Medina Sergio Daniel

---

## Autores

- Lucas Morales - comision ACN4AV 

---
