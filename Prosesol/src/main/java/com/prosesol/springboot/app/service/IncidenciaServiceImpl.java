package com.prosesol.springboot.app.service;

import java.util.List;

import com.prosesol.springboot.app.entity.custom.IncidenciaCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prosesol.springboot.app.entity.Incidencia;
import com.prosesol.springboot.app.entity.dao.IIncidenciaDao;

@Service
public class IncidenciaServiceImpl implements IIncidenciaService{

	@Autowired
	private IIncidenciaDao incidenciaDao;
	
	@Override
	@Transactional(readOnly = true)
	public List<Incidencia> findAll() {
		return (List<Incidencia>)incidenciaDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public Incidencia findById(Long id) {
		return incidenciaDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void save(Incidencia incidencia) {
		incidenciaDao.save(incidencia);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		incidenciaDao.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Incidencia> getIncidenciasByUserId(Long id) {
		return incidenciaDao.getIncidenciasByUserId(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<IncidenciaCustom> getHistorialIncidenciaByIdAfiliado(Long id) {
		return incidenciaDao.getHistorialIncidenciaByIdAfiliado(id);
	}

	@Override
	public Integer getAllIncidenciasACtivas() {
		return incidenciaDao.getAllIncidenciasACtivas();
	}

	@Override
	public Integer getAllIncidenciasEnProceso() {
		return incidenciaDao.getAllIncidenciasEnProceso();
	}

	@Override
	public Integer getAllIncidenciasCompletadas() {
		return incidenciaDao.getAllIncidenciasCompletadas();
	}

	@Override
	public Integer getAllIncidenciasCanceladas() {
		return incidenciaDao.getAllIncidenciasCanceladas();
	}

}
