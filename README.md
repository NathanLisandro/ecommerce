# 🛒 E-commerce API - Sistema de Gerenciamento de Pedidos e Produtos

## 📋 Sobre o Projeto

API REST completa para gerenciamento de um e-commerce, desenvolvida em **Java 17** com **Spring Boot**, incluindo autenticação JWT, CRUD de produtos, sistema de pedidos e relatórios analíticos.

## 🎯 Funcionalidades Implementadas

### ✅ Autenticação e Autorização
- **JWT Token** para autenticação segura
- **Dois perfis de usuário:**
  - `ADMIN` → Pode criar, atualizar e deletar produtos + acesso total
  - `USER` → Pode criar pedidos e visualizar produtos

### ✅ Gerenciamento de Produtos
- **CRUD Completo** com validações
- **Campos:** ID (UUID), Nome, Descrição, Preço, Categoria, Quantidade em Estoque, Datas
- **Filtros:** Por nome, categoria, apenas em estoque
- **Paginação** em todas as listagens

### ✅ Sistema de Pedidos
- **Pedidos com múltiplos produtos**
- **Status:** PENDENTE → APROVADO/REPROVADO/CANCELADO
- **Controle de estoque** automático após pagamento
- **Validações:** Estoque disponível, produtos existentes
- **Cancelamento automático** se estoque insuficiente

### ✅ Relatórios e Analytics
- **Top 5 clientes** que mais compraram (valor total)
- **Ticket médio** por cliente
- **Faturamento mensal** com filtros por ano/mês
- **Consultas SQL otimizadas** para performance

## 🛠 Tecnologias Utilizadas

- **Java 17**
- **Spring Boot 3.5.5**
  - Spring Security (JWT)
  - Spring Data JPA
  - Spring Web
  - Spring Validation
- **MySQL 8.0**
- **Docker & Docker Compose**
- **Lombok**
- **JJWT** (JSON Web Token)

## 🏗 Arquitetura

```
src/main/java/br/com/nathan/ecommerce/
├── config/           # Configurações (JPA Auditing, etc)
├── controller/       # Controllers REST
├── domain/          # Entidades JPA
├── dto/             # Data Transfer Objects
├── enums/           # Enumerações
├── exception/       # Tratamento global de exceções
├── mapper/          # Conversores Entity ↔ DTO
├── repository/      # Repositórios JPA
├── security/        # Configurações de segurança
└── service/         # Regras de negócio
```

## 🚀 Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados
- Porta 8080 disponível

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd ecommerce
```

### 2. Execute com Docker
```bash
docker-compose up --build
```

### 3. A API estará disponível em
```
http://localhost:8080
```

## 📡 Endpoints da API

### 🔐 Autenticação
```http
POST /auth/register    # Registrar usuário
POST /auth/login       # Fazer login
```

### 📦 Produtos
```http
GET    /api/produtos                    # Listar produtos (público)
GET    /api/produtos/{id}               # Buscar produto por ID
GET    /api/produtos/categorias         # Listar categorias
POST   /api/produtos                    # Criar produto (ADMIN)
PUT    /api/produtos/{id}               # Atualizar produto (ADMIN)
DELETE /api/produtos/{id}               # Deletar produto (ADMIN)
PATCH  /api/produtos/{id}/estoque       # Atualizar estoque (ADMIN)
```

### 🛍 Pedidos
```http
GET    /api/pedidos                     # Listar todos pedidos (ADMIN)
GET    /api/pedidos/{id}                # Buscar pedido por ID
GET    /api/pedidos/cliente/{clienteId} # Pedidos do cliente
POST   /api/pedidos                     # Criar pedido
POST   /api/pedidos/{id}/pagamento      # Processar pagamento
PUT    /api/pedidos/{id}/cancelar       # Cancelar pedido
```

### 📊 Relatórios
```http
GET /api/relatorios/top-clientes        # Top 5 clientes (ADMIN)
GET /api/relatorios/ticket-medio        # Ticket médio por cliente (ADMIN)
GET /api/relatorios/faturamento-mensal  # Faturamento mensal (ADMIN)
```

## 📝 Exemplos de Uso

### 1. Registrar Usuário
```json
POST /auth/register
{
    "login": "admin",
    "password": "123456",
    "role": "ADMIN"
}
```

### 2. Fazer Login
```json
POST /auth/login
{
    "login": "admin",
    "password": "123456"
}
```

### 3. Criar Produto
```json
POST /api/produtos
Authorization: Bearer {token}
{
    "nome": "Smartphone XYZ",
    "descricao": "Smartphone com 128GB",
    "preco": 899.99,
    "categoria": "Eletrônicos",
    "quantidadeEstoque": 50
}
```

### 4. Criar Pedido
```json
POST /api/pedidos
Authorization: Bearer {token}
{
    "clienteId": "uuid-do-cliente",
    "itens": [
        {
            "produtoId": "uuid-do-produto",
            "quantidade": 2
        }
    ]
}
```

## 🎲 Banco de Dados

### Estrutura das Tabelas
- `tb_user` - Usuários do sistema
- `tb_cliente` - Clientes (compradores)
- `tb_produtos` - Catálogo de produtos
- `tb_pedido` - Pedidos realizados
- `tb_item_pedido` - Itens de cada pedido

### UUIDs como Chaves Primárias
- Todas as entidades usam **UUID** como identificador
- Configurado como **VARCHAR(36)** no MySQL
- Geração automática pelo Hibernate

## 🔒 Segurança

- **JWT Token** com expiração de 2 horas
- **BCrypt** para hash de senhas
- **Autorização baseada em roles** (ADMIN/USER)
- **Validação de entrada** em todos os endpoints
- **Tratamento global de exceções**

## 📈 Performance

- **Consultas otimizadas** com JPA/JPQL
- **Paginação** em listagens grandes
- **Lazy Loading** em relacionamentos
- **Índices** em campos de busca frequente
- **Connection pooling** com HikariCP

## 🧪 Funcionalidades de Negócio

### Controle de Estoque
- ✅ Verificação de estoque ao criar pedido
- ✅ Redução automática após pagamento aprovado
- ✅ Cancelamento se estoque insuficiente

### Status do Pedido
- **PENDENTE** → Criado, aguardando pagamento
- **APROVADO** → Pago com sucesso, estoque reduzido
- **REPROVADO** → Pagamento rejeitado
- **CANCELADO** → Cancelado pelo usuário ou sistema

### Simulação de Pagamento
- 90% de aprovação automática
- Validação de estoque no momento do pagamento
- Transações atômicas para consistência

## 📊 Consultas Analíticas

### 1. Top 5 Clientes
```sql
SELECT c.id, c.nome, COUNT(p.id) as totalPedidos, SUM(p.valorTotal) as valorTotal
FROM Pedido p JOIN p.cliente c 
WHERE p.status = 'APROVADO' 
GROUP BY c.id, c.nome 
ORDER BY SUM(p.valorTotal) DESC
```

### 2. Ticket Médio
```sql
SELECT c.id, c.nome, AVG(p.valorTotal) as ticketMedio
FROM Pedido p JOIN p.cliente c 
WHERE p.status = 'APROVADO' 
GROUP BY c.id, c.nome
```

### 3. Faturamento Mensal
```sql
SELECT SUM(p.valorTotal) FROM Pedido p 
WHERE p.status = 'APROVADO' 
AND YEAR(p.dataCadastro) = :ano 
AND MONTH(p.dataCadastro) = :mes
```

## 🐳 Docker

### Serviços
- **MySQL 8.0** - Banco de dados
- **Spring Boot App** - API REST

### Configurações
- **Rede interna** para comunicação
- **Volume persistente** para dados do MySQL
- **Health check** para dependências
- **Auto-restart** em caso de falha

## 🧪 Testes

### Cobertura Completa de Testes

O projeto inclui uma suíte completa de testes para garantir qualidade e confiabilidade:

#### 🔬 Testes Unitários (JUnit 5 + Mockito)
- **TokenServiceTest** - Geração e validação de JWT
- **ProdutoServiceTest** - Lógica de negócio de produtos
- **PedidoServiceTest** - Sistema de pedidos e pagamento
- **AuthenticationServiceTest** - Autenticação e registro

#### 🏗 Testes de Integração (DBUnit)
- **ProdutoControllerIntegrationTest** - Endpoints de produtos
- **PedidoControllerIntegrationTest** - Endpoints de pedidos
- **RelatorioControllerIntegrationTest** - Endpoints de relatórios
- **AuthenticationControllerIntegrationTest** - Endpoints de auth

#### 🗄 Testes de Repositório (@DataJpaTest)
- **ProdutoRepositoryTest** - Consultas JPA de produtos
- **PedidoRepositoryTest** - Consultas JPA de pedidos

### Executar Testes

```bash
# Executar todos os testes
mvn test

# Executar apenas testes unitários
mvn test -Dtest="**/*Test"

# Executar apenas testes de integração
mvn test -Dtest="**/*IntegrationTest"

# Gerar relatório de cobertura
mvn jacoco:report
```

### Configuração de Testes

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Datasets DBUnit

- `usuarios.xml` - Dados de usuários para testes
- `clientes.xml` - Dados de clientes
- `produtos.xml` - Catálogo de produtos
- `pedidos.xml` - Pedidos e itens

## 🔧 Configurações

### application-docker.yml
```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/ecommerce
  jpa:
    hibernate:
      ddl-auto: create-drop
      dialect: MySQL8Dialect
      id.new_generator_mappings: false
    properties:
      hibernate:
        type.preferred_uuid_jdbc_type: VARCHAR
```

## 📚 Melhorias Implementadas

1. **DTOs** para separação de responsabilidades
2. **Mappers** para conversão Entity ↔ DTO
3. **Validações** com Bean Validation
4. **Tratamento global** de exceções
5. **Auditoria JPA** (datas de criação/atualização)
6. **Paginação** e filtros avançados
7. **Consultas otimizadas** para relatórios
8. **Segurança** com method-level authorization
9. **Transações** para consistência de dados
10. **Documentação** completa da API
11. **Testes unitários** completos (JUnit 5 + Mockito)
12. **Testes de integração** com DBUnit
13. **Cobertura de testes** em todas as camadas

## 🎯 Conclusão

Este projeto atende completamente aos requisitos do desafio técnico, implementando:

- ✅ **Autenticação JWT** com perfis ADMIN/USER
- ✅ **CRUD completo** de produtos
- ✅ **Sistema de pedidos** com múltiplos produtos
- ✅ **Controle de estoque** automático
- ✅ **Pagamento** com validações
- ✅ **Consultas SQL otimizadas** para relatórios
- ✅ **Arquitetura robusta** e escalável
- ✅ **Documentação completa**

O sistema está pronto para produção com todas as funcionalidades solicitadas! 🚀
