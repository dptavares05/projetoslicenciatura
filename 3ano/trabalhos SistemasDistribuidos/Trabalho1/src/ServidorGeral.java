import java.util.Properties;
// ...

public class ServidorGeral {

    public static void main(String[] args) {
        try {
            Properties p = Config.get();

            String dbHost = p.getProperty("db.host");
            String dbName = p.getProperty("db.name");
            String dbSchema = p.getProperty("db.schema");
            String dbUser = p.getProperty("db.user");
            String dbPass = p.getProperty("db.password");

            String rmiHost = p.getProperty("rmi.host");
            int rmiPort = Integer.parseInt(p.getProperty("rmi.port", "1099"));

            int adminPort = Integer.parseInt(p.getProperty("admin.port", "6000"));

            // GestorBD a partir da config
            GestorBD gestorBD = new GestorBD(
                    dbHost,
                    dbName,
                    dbSchema,
                    dbUser,
                    dbPass);

            // objeto remoto
            IGestorGeral gestorRemoto = new GestorGeralImpl(gestorBD);

            // RMI registry
            java.rmi.registry.LocateRegistry.createRegistry(rmiPort);
            java.rmi.Naming.rebind("rmi://" + rmiHost + ":" + rmiPort + "/GestorGeral", gestorRemoto);
            System.out.println("Servidor Geral RMI pronto em " + rmiHost + ":" + rmiPort);

            // arrancar servidor admin TCP
            ServidorAdminTCP servidorAdmin = new ServidorAdminTCP(gestorBD, adminPort);
            new Thread(servidorAdmin).start();
            System.out.println("Servidor Admin TCP pronto na porta " + adminPort);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro a iniciar o servidor.");
        }
    }
}
