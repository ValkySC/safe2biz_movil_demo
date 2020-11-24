package pe.dominiotech.movil.safe2biz.service;

import pe.dominiotech.movil.safe2biz.base.dao.UsuarioDao;
import pe.dominiotech.movil.safe2biz.model.Usuario;

public class UsuarioService extends AppService{
	
	private UsuarioDao usuarioDao;

	public void setUsuarioDao(UsuarioDao usuarioDao) {
		this.usuarioDao = usuarioDao;
	}
	
	public int save(Usuario usuario) {
		return usuarioDao.save(usuario);
	}
	
	public int update(Usuario usuario) {
		return usuarioDao.update(usuario);
	}
	
	public Usuario getBean(Usuario usuario){
		return usuarioDao.getBean(usuario);
	}
}
