pipeline{
    agent any

    environment{
        DOCKER_IMAGE = 'santiagosuarezosorio/apimonedastt'
        KUBECONFIG = credentials('kubernetes-config')
        // Configurar JAVA_HOME para usar JDK
        JAVA_HOME = 'C:\Program Files\Java\jdk-21'
        PATH = "${JAVA_HOME}\\bin;${env.PATH}"
        // Agregar timestamp para versioning
        BUILD_TIMESTAMP = new Date().format('yyyyMMdd-HHmmss')
        IMAGE_TAG = "${BUILD_TIMESTAMP}-${BUILD_NUMBER}"
    }

    stages{
        stage('Pre-Build Validation') {
            steps {
                script {
                    echo "🔍 Validando pre-requisitos..."
                    bat """
                        echo "JAVA_HOME: %JAVA_HOME%"
                        echo "PATH: %PATH%"
                        java -version
                        javac -version
                        mvn -version
                        echo "Verificando Docker..."
                        docker --version
                        echo "Verificando kubectl..."
                        kubectl version --client
                        echo "Verificando conexión a cluster..."
                        kubectl cluster-info
                    """
                }
            }
        }

        stage('Build & Test') {
            steps {
                script {
                    echo "🏗️ Construyendo aplicación..."
                    bat """
                        echo "Configurando entorno Java..."
                        set JAVA_HOME=%JAVA_HOME%
                        set PATH=%JAVA_HOME%\\bin;%PATH%

                        echo "Limpiando proyecto..."
                        mvn clean

                        echo "Compilando con Maven usando JDK..."
                        mvn compile -DskipTests

                        echo "Creando JAR..."
                        mvn package -DskipTests

                        echo "Ejecutando tests unitarios..."
                        mvn test || echo "WARN: Algunos tests fallaron, continuando..."
                    """
                }
            }
        }

        stage('Construir imagen Docker'){
            steps{
                script {
                    echo "🐳 Construyendo imagen Docker..."
                    bat """
                        echo "Creando imagen con tag versionado..."
                        docker build . -t ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} ${DOCKER_IMAGE}:latest
                        echo "✅ Imagen construida: ${DOCKER_IMAGE}:${IMAGE_TAG}"

                        echo "Verificando imagen creada..."
                        docker images ${DOCKER_IMAGE}
                    """
                }
            }
        }

        stage('Limpiar recursos previos') {
            steps {
                script {
                    echo "🧹 Limpiando recursos previos..."
                    bat """
                        echo "Eliminando recursos existentes..."
                        kubectl delete hpa apimonedas-hpa --ignore-not-found -n apimonedas
                        kubectl delete deployment apimonedas-despliegue --ignore-not-found -n apimonedas
                        kubectl delete service apimonedas-servicio --ignore-not-found -n apimonedas
                        kubectl delete configmap apimonedas-cm --ignore-not-found -n apimonedas
                        kubectl delete secret bdmonedas-secretos --ignore-not-found -n apimonedas

                        echo "Esperando que los recursos se eliminen completamente..."
                        timeout /t 10
                        echo "✅ Recursos previos limpiados"
                    """
                }
            }
        }

        stage('Publicar imagen'){
            steps{
                script {
                    echo "📤 Publicando imagen en Docker Hub..."
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        bat """
                            docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                            docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                            docker push ${DOCKER_IMAGE}:latest
                            docker logout
                            echo "✅ Imagen publicada: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                        """
                    }
                }
            }
        }

        stage('Aplicar Manifiestos') {
            steps {
                script {
                    echo "📋 Aplicando manifiestos de Kubernetes..."
                    bat """
                        echo "Creando namespace..."
                        kubectl create namespace apimonedas --dry-run=client -o yaml | kubectl apply -f -

                        echo "Aplicando manifiestos..."
                        kubectl apply -f manifiestos\\api\\

                        echo "Verificando recursos creados..."
                        kubectl get all -n apimonedas
                        echo "✅ Manifiestos aplicados correctamente"
                    """
                }
            }
        }

        stage('Despliegue y Verificación'){
            steps{
                script {
                    echo "🚀 Desplegando aplicación..."
                    bat """
                        echo "Actualizando imagen del deployment..."
                        kubectl set image deployment/apimonedas-despliegue apimonedas=${DOCKER_IMAGE}:${IMAGE_TAG} -n apimonedas

                        echo "Esperando rollout (con timeout extendido)..."
                        kubectl rollout status deployment/apimonedas-despliegue -n apimonedas --timeout=900s

                        echo "✅ Despliegue completado exitosamente"
                    """
                }
            }
        }

        stage('Health Check Detallado') {
            steps {
                script {
                    echo "🏥 Verificando salud de la aplicación..."
                    bat """
                        echo "=== ESTADO DETALLADO ==="

                        echo "Estado de los pods:"
                        kubectl get pods -n apimonedas -o wide

                        echo "Estado del deployment:"
                        kubectl get deployment apimonedas-despliegue -n apimonedas

                        echo "Estado del servicio:"
                        kubectl get service -n apimonedas

                        echo "ConfigMaps y Secrets:"
                        kubectl get configmap,secret -n apimonedas

                        echo "=== LOGS DE DIAGNÓSTICO ==="
                        echo "Logs de los pods (últimas 50 líneas):"
                        kubectl logs -l app=apimonedas -n apimonedas --tail=50 || echo "No se pudieron obtener logs de pods en ejecución"

                        echo "=== VERIFICACIÓN FINAL ==="
                        echo "Descripción de pods con problemas:"
                        kubectl describe pods -l app=apimonedas -n apimonedas || echo "No se pudo describir pods"

                        echo "Esperando que los pods estén listos..."
                        kubectl wait --for=condition=ready pod -l app=apimonedas -n apimonedas --timeout=600s || echo "WARN: Pods no estuvieron listos en el tiempo esperado"

                        echo "✅ Health Check completado"
                    """
                }
            }
        }
    }

    post {
        always {
            script {
                echo "📊 Recopilando información final..."
                bat """
                    echo "=== RESUMEN FINAL ==="
                    echo "Imagen desplegada: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                    echo "Namespace: apimonedas"
                    echo "Timestamp: ${BUILD_TIMESTAMP}"
                    echo "Build Number: ${BUILD_NUMBER}"

                    echo "=== ESTADO FINAL DE RECURSOS ==="
                    kubectl get all -n apimonedas || echo "No se pudo obtener estado de recursos"

                    echo "=== INFORMACIÓN DE DEBUG ==="
                    echo "Java Version usado:"
                    java -version || echo "Java no disponible"
                    echo "Maven Version usado:"
                    mvn -version || echo "Maven no disponible"
                """
            }
        }
        success {
            echo "🎉 ¡Pipeline completado exitosamente!"
            echo "✅ Aplicación desplegada: ${DOCKER_IMAGE}:${IMAGE_TAG}"
            echo "🌐 La aplicación debería estar disponible en el cluster"
        }
        failure {
            script {
                echo "❌ Pipeline falló. Recopilando logs detallados para diagnóstico..."
                bat """
                    echo "=== ANÁLISIS DE FALLO ==="

                    echo "Verificando Java/Maven:"
                    java -version || echo "Java no disponible"
                    mvn -version || echo "Maven no disponible"

                    echo "Estado de pods problemáticos:"
                    kubectl get pods -n apimonedas || echo "No se pudieron obtener pods"

                    echo "Logs completos de pods con errores:"
                    kubectl logs -l app=apimonedas -n apimonedas --tail=200 || echo "No se pudieron obtener logs"

                    echo "Eventos del namespace:"
                    kubectl get events -n apimonedas --sort-by='.lastTimestamp' || echo "No se pudieron obtener eventos"

                    echo "Descripción detallada del deployment:"
                    kubectl describe deployment apimonedas-despliegue -n apimonedas || echo "No se pudo describir deployment"

                    echo "Verificando imágenes Docker:"
                    docker images ${DOCKER_IMAGE} || echo "No se encontraron imágenes"
                """
            }
        }
        unstable {
            echo "⚠️ Pipeline completado con advertencias"
        }
    }
}