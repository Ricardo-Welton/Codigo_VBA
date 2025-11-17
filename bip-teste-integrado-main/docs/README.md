Projeto de Gerenciamento de Benefícios (Spring Boot + EJB + Angular)

Este projeto implementa uma API RESTful completa com funcionalidade CRUD para gerenciar benefícios, utilizando Spring Boot para o backend e um serviço EJB (simulado/híbrido) para lógica de negócio crítica. O frontend é desenvolvido em Angular.

1. Estrutura do Projeto

backend/: Contém o módulo Spring Boot (API REST, JPA/H2, Service EJB).

frontend/: Contém o componente Angular (beneficios-app.component.ts).

2. Configuração e Execução do Backend (Spring Boot)

Pré-requisitos

JDK 17+

Maven

Inicialização

Navegue até a pasta backend/:

cd backend


Compile o projeto com Maven:

mvn clean install


Execute a aplicação Spring Boot:

mvn spring-boot:run


A API estará disponível em http://localhost:8080.

Documentação (Swagger/OpenAPI)

Após iniciar o backend, a documentação interativa da API estará acessível em:
http://localhost:8080/swagger-ui/index.html

3. Implementações Chave

1. Correção do Bug no BeneficioEjbService

O método transfer em BeneficioEjbService.java foi corrigido para garantir integridade transacional e evitar a condição de corrida (Lost Update) e saldo negativo:

Validação de Saldo: Impede que o saldo de origem fique negativo (from.getValor().compareTo(amount) < 0).

Bloqueio de Concorrência: Uso de LockModeType.PESSIMISTIC_WRITE ao carregar as entidades de origem e destino, garantindo que nenhuma outra transação possa modificar as contas até que esta transação seja concluída.

2. Implementação CRUD + Integração EJB

Entidade/Repositório: Beneficio.java e BeneficioRepository.java fornecem o mapeamento JPA e o acesso básico a dados (H2 em memória).

Serviço EJB: BeneficioEjbService.java implementa a lógica de negócio principal (CRUD e transfer), mantendo as anotações EJB como @Stateless e @PersistenceContext.

Controller: BeneficioController.java expõe os endpoints REST (GET, POST, PUT, DELETE, e POST /transfer).

3. Implementação de Testes

O arquivo BackendApplicationTests.java contém um teste de integração (testTransferBugFixWithPessimisticLocking) que simula 100 transferências simultâneas usando um ExecutorService. Este teste valida que o bloqueio pessimista funcionou corretamente, garantindo que o saldo final seja exatamente o esperado, prevenindo o problema de Lost Update.

4. Submissão (Fork + PR)

O processo de submissão via Fork e Pull Request (PR) deve seguir os seguintes passos no seu ambiente Git (ex: GitHub):

Fork o repositório de destino para sua conta.

Clone seu fork localmente.

Adicione os arquivos gerados (seguindo a estrutura backend/ e frontend/).

Commit as mudanças com mensagens descritivas.

Push as mudanças para o seu fork remoto.

Acesse o repositório original (ou seu fork) e crie um Pull Request para a branch principal, descrevendo as implementações realizadas conforme este README.
