import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OracleDatabaseSetup {

    private static final String SCHEMA_SQL_1_TABLE =
        "CREATE TABLE BENEFICIO (" +
        "  ID NUMBER(10) PRIMARY KEY," +
        "  NOME VARCHAR2(100) NOT NULL," +
        "  DESCRICAO VARCHAR2(255)," +
        "  VALOR NUMBER(15,2) NOT NULL," +
        "  ATIVO NUMBER(1) DEFAULT 1," +
        "  VERSION NUMBER(10) DEFAULT 0" +
        ")";

    private static final String SCHEMA_SQL_2_SEQUENCE =
        "CREATE SEQUENCE beneficio_seq START WITH 1 INCREMENT BY 1";
    
        private static final String SEED_SQL =
        "INSERT INTO BENEFICIO (NOME, DESCRICAO, VALOR, ATIVO) VALUES" +
        "('Beneficio A', 'Descrição A', 1000.00, 1)";
    
    private static final String SEED_SQL_2 =
        "INSERT INTO BENEFICIO (NOME, DESCRICAO, VALOR, ATIVO) VALUES" +
        "('Beneficio B', 'Descrição B', 500.00, 1)";

    public static void main(String[] args) {
        String jdbcUrl = "jdbc:oracle:thin:@//SEU_HOST:PORTA/SEU_SERVICE_NAME"; 
        String user = "SEU_USUARIO"; 
        String password = "SUA_SENHA";
        // ------------------------------------------

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("✅ Conexão estabelecida com o Oracle Database.");
            conn.setAutoCommit(false); // Iniciar

            // Criar Tabela)
            System.out.println("➡️ Executando SQL: Criando tabela BENEFICIO...");
            stmt.execute(SCHEMA_SQL_1_TABLE);
            System.out.println("✅ Tabela BENEFICIO criada.");

            // Criação da Sequence
            System.out.println("➡️ Executando SQL: Criando SEQUENCE beneficio_seq...");
            stmt.execute(SCHEMA_SQL_2_SEQUENCE);
            System.out.println("✅ SEQUENCE criada.");
            
            // Inserir Dados)
            System.out.println("➡️ Executando SQL: Inserindo dados iniciais (Seed)...");
            stmt.executeUpdate(SEED_SQL);
            stmt.executeUpdate(SEED_SQL_2);
            System.out.println("✅ 2 registros inseridos com sucesso.");
            
            conn.commit(); 

            // Consultar os Dados
            System.out.println("\n📋 Verificando os dados inseridos:");
            String query = "SELECT ID, NOME, VALOR, ATIVO FROM BENEFICIO";
            try (ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    long id = rs.getLong("ID");
                    String nome = rs.getString("NOME");
                    double valor = rs.getDouble("VALOR");
                    int ativo = rs.getInt("ATIVO");
                    System.out.printf("   ID: %d | Nome: %s | Valor: %.2f | Ativo: %d%n", id, nome, valor, ativo);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Erro ao executar o SQL/JDBC para Oracle: " + e.getMessage());
            e.printStackTrace();
        }
    }
}