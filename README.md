![CrediYa](docs/CrediYa_logo.png)


# CrediYa - crediya-loan-application-service

Este microservicio está diseñado para gestionar solicitudes de credito dentro del sistema CreiYa, siguiendo principios de arquitectura hexagonal, desarrollado con WebFlux para un enfoque reactivo y eficiente en la gestión de solicitudes concurrentes.

Cada microservicio en el ecosistema de CreiYa se mantiene en un repositorio y base de datos independiente, asegurando modularidad y escalabilidad.


# Tecnologías utilizadas

- Java 17 / Spring Boot WebFlux – Desarrollo reactivo y no bloqueante.
- Arquitectura Hexagonal (scaffold) – Separación clara entre dominio, aplicación e infraestructura.
- Gradle – Gestión de dependencias y construcción del proyecto.
- PostgreSQL – Base de datos relacional robusta y escalable.
- Spring Data R2DBC – Acceso reactivo a bases de datos SQL.
- Swagger / OpenAPI – Documentación de API interactiva.
- SonarLint – Validación de calidad de código en tiempo de desarrollo.
- JUnit + Mockito / Test unitarios – Validación de lógica de negocio.
- Logs de traza y manejo de excepciones – Para monitoreo y control de errores.


# Arquitectura
Para este proyecto se ha utilizado una clean architecture  (utilizando el pluggin de bancolombia scaffold), que se compone de las siguientes capas: .-


![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)
- Domain
- Infrastructure
- Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

# Base de datos

Para la base de datos se utiliza PostgreSQL en Supabase, y se gestiona a través de R2DBC para mantener el enfoque reactivo en todas las capas del microservicio. La configuración de la conexión a la base de datos se encuentra en el archivo `application.yml`, donde se especifican los detalles necesarios para establecer la conexión.
![CrediYa](docs/BD_loan-application-service.png)

# Notificaciones Aprobación/Rechazo
El microservicio de solicitud de crédito se integra con cola en SQS para enviar notificaciones de aprobación o rechazo de solicitudes de crédito. Cuando una solicitud es procesada, el microservicio publica un mensaje en la cola SQS correspondiente, que luego puede ser consumido por otros servicios responsables de enviar las notificaciones a los usuarios finales al momento que el asesor actualiza el estado de su solicitud.

Esta SQS se integra con una lambda que se encarga de enviar el correo electrónico al cliente notificándole sobre el estado de su solicitud de crédito mediante SES.


![CrediYa](docs/SQS_Notification.png)
![CrediYa](docs/Lambda_Notification.png)
