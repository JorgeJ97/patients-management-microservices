#!/bin/bash
set -e

# Configuración de entorno
export AWS_ACCESS_KEY_ID="test"
export AWS_SECRET_ACCESS_KEY="test"
export AWS_DEFAULT_REGION="us-east-1"
export AWS_ENDPOINT_URL="http://localhost:4566"

# Variables
STACK_NAME="patient-management"
TEMPLATE_FILE="./cdk.out/localstack.template.json"
BUCKET_NAME="cf-temp-$(date +%s | md5sum | head -c 8)"

# 1. Verificar conexión con LocalStack
echo "Verificando conexión con LocalStack..."
if ! curl -s http://localhost:4566/health >/dev/null; then
    echo "ERROR: LocalStack no responde en localhost:4566"
    exit 1
fi

# 2. Determinar estrategia de despliegue basada en tamaño
TEMPLATE_SIZE=$(stat -c%s "$TEMPLATE_FILE")
if [ $TEMPLATE_SIZE -gt 51200 ]; then
    echo "Template grande detectado ($TEMPLATE_SIZE bytes), usando S3..."

    # Crear bucket y subir template
    aws s3api create-bucket --bucket $BUCKET_NAME >/dev/null
    aws s3 cp "$TEMPLATE_FILE" "s3://$BUCKET_NAME/template.json" >/dev/null

    # Ejecutar despliegue con S3
    aws cloudformation deploy \
        --stack-name "$STACK_NAME" \
        --s3-bucket "$BUCKET_NAME" \
        --template-file "$TEMPLATE_FILE" \
        --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
        --no-fail-on-empty-changeset \
        --endpoint-url="$AWS_ENDPOINT_URL"

    # Limpieza
    aws s3 rb "s3://$BUCKET_NAME" --force >/dev/null 2>&1
else
    echo "Template pequeño ($TEMPLATE_SIZE bytes), usando archivo local..."
    aws cloudformation deploy \
        --stack-name "$STACK_NAME" \
        --template-file "$TEMPLATE_FILE" \
        --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM \
        --no-fail-on-empty-changeset \
        --endpoint-url="$AWS_ENDPOINT_URL"
fi

# 3. Mostrar resultados
echo -e "\n✅ Despliegue completado. Outputs:"
aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --query "Stacks[0].Outputs" \
    --output table \
    --endpoint-url="$AWS_ENDPOINT_URL" || echo "No se encontraron outputs"

echo -e "\n🔗 DNS del Load Balancer:"
aws elbv2 describe-load-balancers \
    --query "LoadBalancers[0].DNSName" \
    --output text \
    --endpoint-url="$AWS_ENDPOINT_URL" || echo "No se encontró Load Balancer"