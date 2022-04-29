package pe.dominiotech.movil.safe2biz.base.dao;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pe.dominiotech.movil.safe2biz.exception.LoginException;
import pe.dominiotech.movil.safe2biz.model.Usuario;

public class UsuarioDao extends SQLiteHelper{

	public UsuarioDao(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	

	public int save(Usuario usuario) {
		int isSaveUsuario = 1;
		try {
		 	RuntimeExceptionDao<Usuario, String> usuarioDao = createDao(Usuario.class);
		 	usuarioDao.createOrUpdate(usuario);

		 	return isSaveUsuario;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("UsuarioDao-save.error");
		}
	}
	
	public int update(Usuario usuario) {
		try {
			RuntimeExceptionDao<Usuario, String> usuarioDao = createDao(Usuario.class);
			usuario.setUsuario_id(1);
			int row = usuarioDao.update(usuario);
			return row;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new LoginException("usuarioService-usuarioDao-update.error");
		}
	}
	
		
	public Usuario getBean(Usuario usuario) {
		RuntimeExceptionDao<Usuario, String> usuarioDao = createDao(Usuario.class);
		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("usuario_id", usuario.getUsuario_id());
//		fields.put("password", usuario.getPassword());
//		fields.put("ip_o_dominio_servidor", usuario.getIpOrDominioServidor());
		List<Usuario> usuarioList = usuarioDao.queryForFieldValues(fields);
		if(usuarioList != null && usuarioList.size() > 0){
			usuario = usuarioList.get(0);
		}else{
			return usuario;
		}

		return usuario;
	}


}
