#  Patients Management Microservices

Este proyecto es una **API basada en microservicios** para la gestión de pacientes. Simula un entorno real de despliegue en AWS utilizando herramientas modernas como Kafka, Docker, AWS, LocalStack (para simular entorno de AWS de manera local), entre otras. Cada microservicio tiene su propio contenedor, igual que las instancias de bases de datos y el cluster de kafka, lo que asegura su disponibilidad gracias a los servicios de despliegue de AWS.

---

## 🧩 Infraestructura en AWS
![Infrastructure](./infrastructure/assets/Patients-Management-Infrastructure-diagram.drawio.png)

## 📌 Tabla de contenidos

- [🚀 Características](#-características)
- [🛠️ Tecnologías](#-tecnologías)
- [📦 Microservicios](#-microservicios)
- [☁️ Despliegue](#-despliegue)
- [🧪 Tests](#-tests)
- [💻 Ejecutar localmente](#-ejecutar-localmente)
- [📖 Documentacion API](#-documentacionAPI)
---

## 🚀 Características

- Gestión completa de pacientes (CRUD).
- Autenticación y autorización segura con JWT.
- Comunicación entre servicios con **Apache Kafka**.
- Despliegue automatizado con **AWS CDK**.
- Emulación local de la nube con **LocalStack**.

---


## 🛠️ Tecnologías

- **Java & Spring Boot**
- **Kafka (MSK)**
- **gRPC**
- **PostgreSQL (RDS)**
- **Docker** (Para la construccion de las imagenes de cada microservicio)
- **AWS CDK** (Infraestructura como código)
- **ECS + Fargate** (Ejecución de contenedores)
- **Secrets Manager**
- **Route 53** (Health checks para las bases de datos)
- **ALB (Application Load Balancer)**
- **LocalStack** (Simulación de servicios AWS)
- **JUnit** (Para los test de integración)
- **Swagger ui** (Para documentacion de la API)

---

## 📦 Microservicios

| Servicio           | Descripción                                           |
|--------------------|-------------------------------------------------------|
| `patients-service` | Manejo CRUD de pacientes                              |
| `auth-service`     | Registro, login, emisión y validación de tokens JWT  |
| `billing-service`  | Procesamiento de pagos y facturación                  |
| `analytics-service`| Reportes y estadísticas basadas en eventos de Kafka  |
| `api-gateway`      | Punto de entrada unificado para todos los servicios  |

---

## ☁️ Despliegue

### 🧱 Infraestructura con CDK

Se utiliza AWS CDK para definir los siguientes recursos:

- Contenedores en ECS + Fargate
- Base de datos PostgreSQL con RDS
- Kafka con MSK
- Secrets en AWS Secrets Manager
- Balanceador de carga (ALB)
- Health checks con Route 53


---

## 🧪 Tests

- Pruebas de integración con **JUnit 5**.

---





