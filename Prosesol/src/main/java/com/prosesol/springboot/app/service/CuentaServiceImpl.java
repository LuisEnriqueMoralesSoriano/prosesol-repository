package com.prosesol.springboot.app.service;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.prosesol.springboot.app.entity.Cuenta;
import com.prosesol.springboot.app.entity.dao.ICuentaDao;

@Service
public class CuentaServiceImpl implements ICuentaService{

	@Autowired
	private ICuentaDao cuentaDao;
	
	@Override
	@Transactional(readOnly = true)
	public List<Cuenta> findAll() {
		return (List<Cuenta>)cuentaDao.findAll();
	}

	@Override
	@Transactional
	public void save(Cuenta cuenta) {
		cuentaDao.save(cuenta);
	}


	@Override
	@Transactional(readOnly = true)
	public Cuenta findById(Long id) {
		return cuentaDao.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public void delete(Long id) {
		cuentaDao.deleteById(id);
		
	}

	@Override
	public String[] getVariablesCuenta() {

		Field campos[] = Cuenta.class.getDeclaredFields();
		String variablesCuenta[] = new String[campos.length];
		
		for(int i = 0; i < campos.length; i++) {
			variablesCuenta[i] = campos[i].getName();
		}
		
		return variablesCuenta;
		
	}

}
