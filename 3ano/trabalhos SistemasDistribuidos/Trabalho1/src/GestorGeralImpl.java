import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class GestorGeralImpl extends UnicastRemoteObject implements IGestorGeral {

    private final GestorBD gestorBD;

    public GestorGeralImpl(GestorBD gestorBD) throws RemoteException {
        super();
        this.gestorBD = gestorBD;
    }

    // --- UTILIZADORES ---
    @Override
    public void registarUtilizador(String nome, String email, String tipo) throws RemoteException {
        try {
            gestorBD.registarUtilizador(nome, email, tipo);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public List<String> historicoUtilizador(int idUtilizador) throws RemoteException {
        try {
            return gestorBD.historicoUtilizador(idUtilizador);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    // --- RECURSOS ---

    @Override
    public List<String> listarRecursosFiltrado(String tipo, String estadoOp, String estadoAdmin, String texto)
            throws RemoteException {
        try {
            return gestorBD.listarRecursosFiltrado(nullIfBlank(tipo), nullIfBlank(estadoOp),
                    nullIfBlank(estadoAdmin), nullIfBlank(texto));
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // --- EMPRÉSTIMOS ---
    @Override
    public List<String> listarEmprestimosAtivos() throws RemoteException {
        try {
            return gestorBD.listarEmprestimosAtivos();
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void criarEmprestimo(int idUtilizador, int idRecurso, int duracaoDias) throws RemoteException {
        try {
            gestorBD.criarEmprestimo(idUtilizador, idRecurso, duracaoDias);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void devolverEmprestimo(int idEmprestimo) throws RemoteException {
        try {
            gestorBD.devolverEmprestimo(idEmprestimo);
        } catch (Exception e) {
            throw new RemoteException(e.getMessage(), e);
        }
    }

}
