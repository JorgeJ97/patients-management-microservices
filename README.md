# 🏥 Patients Management Microservices

Este proyecto es una **API basada en microservicios** para la gestión de pacientes en un entorno clínico. Está diseñada para escalar, ser segura y simular un entorno real de despliegue en AWS utilizando herramientas modernas como Kafka, Docker, CDK, LocalStack, entre otras.

---

## 📌 Tabla de contenidos

- [🚀 Características](#-características)
- [🧩 Arquitectura](#-arquitectura)
- [🛠️ Tecnologías](#-tecnologías)
- [📦 Microservicios](#-microservicios)
- [☁️ Despliegue](#-despliegue)
- [🧪 Tests](#-tests)
- [💻 Ejecutar localmente](#-ejecutar-localmente)
- [🖼️ Capturas de pantalla](#-capturas-de-pantalla)
- [📄 Licencia](#-licencia)

---

## 🚀 Características

- Gestión completa de pacientes (CRUD).
- Autenticación y autorización segura con JWT.
- Sistema de facturación.
- Análisis de datos con microservicio de analytics.
- Comunicación entre servicios con **Apache Kafka**.
- Despliegue automatizado con **AWS CDK**.
- Emulación local de la nube con **LocalStack**.

---

## 🧩 Arquitectura

```txt
           Usuario
              |
              v
        ┌────────────┐
        │API Gateway │
        └─────┬──────┘
              |
 ┌────────────┼────────────┐
 ↓            ↓            ↓
Auth      Patients     Billing
Service   Service      Service
  ↓            ↓            ↓
PostgreSQL  PostgreSQL   Kafka (MSK)
              ↓
          Analytics
           Service
```

---

## 🛠️ Tecnologías

- **Java & Spring Boot**
- **Kafka (MSK)**
- **PostgreSQL (RDS)**
- **Docker**
- **AWS CDK** (Infraestructura como código)
- **ECS + Fargate** (Ejecución de contenedores)
- **Secrets Manager**
- **Route 53** (Health checks)
- **ALB (Application Load Balancer)**
- **LocalStack** (Simulación de servicios AWS)
- **JUnit / Testcontainers** (Testing)

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

### 🧪 Simulación con LocalStack

Se puede simular el entorno AWS localmente usando [LocalStack](https://github.com/localstack/localstack).

```bash
# Ejecutar entorno local
docker-compose up --build
```

---

## 🧪 Tests

- **Testcontainers** para crear contenedores temporales de Kafka/Postgres.
- Pruebas de integración con **JUnit 5**.

---

## 💻 Ejecutar localmente

```bash
# Clonar repositorio
git clone https://github.com/JorgeJ97/patients-management-microservices.git
cd patients-management-microservices

# Levantar entorno local
docker-compose up --build
```

Accede al API Gateway en: [http://localhost:8080](http://localhost:8080)

---

## 🖼️ Capturas de pantalla

> Agrega aquí tus capturas alojadas en `./assets/`

```md
![Arquitectura general](./assets/arquitectura.png)
![Ejemplo en Docker](./assets/docker-demo.png)
```

---

## 📄 Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](./LICENSE) para más detalles.
