pipeline{
    agent any

    environment{
        DOCKER_IMAGE = 'apimonedastt'
        CONTAINER_NAME = 'dockerapimonedastt'
        DOCKER_NETWORK = 'dockermonedas_red'
        HOST_PORT = '9080'
        CONTAINER_PORT = '8080'
    }

    stages{
        /*
        stage('Compilación Maven'){
            steps{
                bat 'mvn clean package -Dskiptests'
            }
        }
        */
        stage('Construir imagen'){
            steps{
                {
                    bat "docker build . -t ${DOCKER_IMAGE}"
                }
            }
        }
        stage('Detener contenedor existente'){
            steps{
                bat "docker stop ${CONTAINER_NAME} || echo 'No se pudo detener el contenedor porque no existe.'"
                bat "docker rm ${CONTAINER_NAME} || echo 'No se pudo eliminar el contenedor porque no existe.'"
            }
        }
        stage('Desplegar contenedor'){
            steps{
                bat "docker run --network ${DOCKER_NETWORK} --name ${CONTAINER_NAME} -p ${HOST_PORT}:${CONTAINER_PORT} -d ${DOCKER_IMAGE}"
            }
        }
    }
}