import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IGestorGeral extends Remote {

    void registarUtilizador(String nome, String email, String tipo) throws RemoteException;

    List<String> listarRecursosFiltrado(String tipo, String estadoOp, String estadoAdmin, String texto)
            throws RemoteException;

    List<String> listarEmprestimosAtivos() throws RemoteException;

    List<String> historicoUtilizador(int idUtilizador) throws RemoteException;

    void criarEmprestimo(int idUtilizador, int idRecurso, int duracaoDias) throws RemoteException;

    void devolverEmprestimo(int idEmprestimo) throws RemoteException;

}
