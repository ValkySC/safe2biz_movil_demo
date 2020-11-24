package pe.dominiotech.movil.safe2biz.base.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

import pe.dominiotech.movil.safe2biz.MainApplication;
import pe.dominiotech.movil.safe2biz.model.Usuario;

public class SynchronizeDao extends SQLiteHelper {
	protected static final String TAG = SynchronizeDao.class.getSimpleName();
	private MainApplication mainApplication;
	//TODO: Capturar
	public SynchronizeDao(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}
	
	public void limpiarBaseDeDatos(String codEquipo) {
		RuntimeExceptionDao<Usuario, String> daoUsuario = createDao(Usuario.class);
		try {
			DeleteBuilder<Usuario, String> deleteUsuarioBuilder = daoUsuario.deleteBuilder();
			deleteUsuarioBuilder.where().eq("cod_usuario", codEquipo);
			deleteUsuarioBuilder.delete();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void guardarDatosUsuario(final List<Usuario> usuarios) {

		if (usuarios == null) {
			return;
		}
		try {
			final RuntimeExceptionDao<Usuario, String> dao = createDao(Usuario.class);
			 DeleteBuilder<Usuario, String> deleteBuilder = dao.deleteBuilder();

			dao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					for (Usuario usuario : usuarios) {
						// escuelaBean.setSincronizar(1);
						dao.createOrUpdate(usuario);
					}
					return null;
				}
			});

		} catch (android.database.SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Se produjo una excepcion en el proceso de Sincronizacion");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Se produjo una excepcion en el proceso de Sincronizacion");

		}
	}
	
}
