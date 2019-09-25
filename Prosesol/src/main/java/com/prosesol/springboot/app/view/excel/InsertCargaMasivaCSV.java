package com.prosesol.springboot.app.view.excel;

import com.josketres.rfcfacil.Rfc;
import com.prosesol.springboot.app.entity.*;
import com.prosesol.springboot.app.service.*;
import com.prosesol.springboot.app.util.CalcularFecha;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class InsertCargaMasivaCSV {

	protected final Log LOG = LogFactory.getLog(InsertCargaMasivaCSV.class);

	@Value("${app.clave}")
	private String clave;

	@Autowired
	private IAfiliadoService afiliadoService;

	@Autowired
	private IPeriodicidadService periodicidadService;

	@Autowired
	private ICuentaService cuentaService;

	@Autowired
	private IPromotorService promotorService;

	@Autowired
	private IServicioService servicioService;

	@Autowired
	private CalcularFecha calcularFechas;

	private int corte;
	private Afiliado titular;

	private String isBeneficiario;
	private String rfcAfiliado;

	private Date fechaNacimiento;

	private LocalDate localDate;

	private DateTimeFormatter dateTimeFormatter;

	private Rfc rfc;

	private Collator collator = Collator.getInstance(new Locale("es"));

	private boolean isValidAfiliado;
	private boolean isValid;
	String log = "";

	public String evaluarDatosList(boolean isVigor, Integer counterLinea, Map<Integer, String> campos,
								   Long idCuentaComercial){

		Afiliado afiliado = new Afiliado();

		isValidAfiliado = true;

		rfc = null;
		boolean isInteger;
		boolean isValid;

		if(campos.size() < 26){
			isBeneficiario = "No";
		}

		for(Map.Entry<Integer, String> campo : campos.entrySet()){
			try {

				switch (campo.getKey()){
					case 0:
						if (campo.getValue().length() == 0) {
							LOG.info(counterLinea + " - " + "El nombre no puede quedar vacío");
							log = counterLinea + " - " + "El nombre no puede quedar vacío";
							isValidAfiliado = false;
						} else {
							afiliado.setNombre(campo.getValue());
							isInteger = isInteger(afiliado.getNombre());
							if(!isInteger){
								LOG.info(counterLinea + " - " + "Nombre: " + afiliado.getNombre());
							}else{
								log = counterLinea + " - " + "El nombre no puede contener valores númericos";
								LOG.info(counterLinea + " - " + "El nombre no puede contener valores númericos");
								isValidAfiliado = false;
							}

						}
						break;
					case 1:
						if (campo.getValue().length() == 0) {
							LOG.info(counterLinea + " - " + "El Apellido Paterno no puede quedar vacío");
							log = counterLinea + " - " + "El Apellido Paterno no puede quedar vacío";
							isValidAfiliado = false;
						} else {
							afiliado.setApellidoPaterno(campo.getValue());
							isInteger = isInteger(afiliado.getApellidoPaterno());

							if(!isInteger){
								LOG.info(counterLinea + " - " + "Apellido Paterno: " + afiliado.getApellidoPaterno());
							}else{
								log = counterLinea + " - " + "El Apellido Paterno no puede contener valores númericos";
								LOG.info(counterLinea + " - " + "El Apellido Paterno no puede contener valores númericos");
								isValidAfiliado = false;
							}
						}
						break;
					case 2:
						if (campo.getValue().length() == 0) {
							LOG.info(counterLinea + " - " + "El Apellido Materno no puede quedar vacío");
							log = counterLinea + " - " + "El Apellido Materno no puede quedar vacío";
							isValidAfiliado = false;
						} else {
							afiliado.setApellidoMaterno(campo.getValue());
							isInteger = isInteger(afiliado.getApellidoMaterno());
							if(!isInteger){
								LOG.info(counterLinea + " - " + "Apellido Materno: " + afiliado.getApellidoMaterno());
							}else{
								LOG.info(counterLinea + " - " + "El Apellido Materno no puede contener valores númericos");
								log = counterLinea + " - " + "El Apellido Materno no puede contener valores númericos";
								isValidAfiliado = false;
							}

						}
						break;
					case 3:
						if(campo.getValue().length() == 0){
							LOG.info(counterLinea + " - " + "La fecha de nacimiento no debe quedar vacío");
							log = counterLinea + " - " + "La fecha de nacimiento no debe quedar vacío";
							isValidAfiliado = false;
						}else{
							isValid = isValidFormat("dd/MM/yyyy", campo.getValue());

							if(isValid){
								afiliado.setFechaNacimiento(new SimpleDateFormat("dd/MM/yyyy").parse(campo.getValue()));
								LOG.info(counterLinea + " - " + "Fecha de Nacimiento: " + afiliado.getFechaNacimiento());
							}else{
								log = counterLinea + " - " + "Formato de fecha incorrecto, formato correcto dd/mm/yyyy";
								LOG.info(counterLinea + " - " + "Formato de fecha incorrecto, formato correcto dd/mm/yyyy");
								isValidAfiliado = false;
							}
						}
						break;
					case 4:
						afiliado.setLugarNacimiento(campo.getValue());
						isInteger = isInteger(afiliado.getLugarNacimiento());
						if(!isInteger){
							LOG.info(counterLinea + " - " + "Lugar de Nacimiento: " + afiliado.getLugarNacimiento());
						}else{
							LOG.info(counterLinea + " - " + "El Lugar de Nacimiento no puede contener valores númericos");
							log = counterLinea + " - " + "El Lugar de Nacimiento no puede contener valores númericos";
							isValidAfiliado = false;
						}
						break;
					case 5:
						afiliado.setEstadoCivil(campo.getValue());
						isInteger = isInteger(afiliado.getEstadoCivil());
						if(!isInteger){
							LOG.info(counterLinea + " - " + "Estado Civil: " + afiliado.getEstadoCivil());
						}else{
							LOG.info(counterLinea + " - " + "El Estado Civil no puede contener valores númericos");
							log = counterLinea + " - " + "El Estado Civil no puede contener valores númericos";
							isValidAfiliado = false;
						}
						break;
					case 6:
						if(campo.getValue().length() == 0){
							afiliado.setNumeroDependientes(null);
							LOG.info(counterLinea + " - " + "Número de Dependientes: " + afiliado.getNumeroDependientes());
						}else{
							afiliado.setNumeroDependientes(Integer.parseInt(campo.getValue()));
							LOG.info(counterLinea + " - " + "Número de Dependientes: " + afiliado.getNumeroDependientes());
						}
						break;
					case 7:
						afiliado.setOcupacion(campo.getValue());
						LOG.info(counterLinea + " - " + "Ocupación: " + afiliado.getOcupacion());
						break;
					case 8:
						if(campo.getValue().toUpperCase().equals("MASCULINO") ||
								campo.getValue().toUpperCase().equals("FEMENINO")){
							afiliado.setSexo(campo.getValue());
							LOG.info(counterLinea + " - " + "Sexo: " + afiliado.getSexo());
						}else{
							LOG.info(counterLinea + " - " + "El Sexo debe ser 'Masculino' ó 'Femenino'");
							log = counterLinea + " - " + "El Sexo debe ser 'Masculino' ó 'Femenino'";
							isValidAfiliado = false;
						}
						break;
					case 9:
						if(campo.getValue().length() == 0){
							afiliado.setPais(null);
							LOG.info(counterLinea + " - " + "País: " + afiliado.getPais());
						}else if(campo.getValue().length() != 3){
							LOG.info(counterLinea + " - " + "El país debe ser de 3 posiciones");
							log = counterLinea + " - " + "El país debe ser de 3 posiciones";
							isValidAfiliado = false;
						}else{
							afiliado.setPais(campo.getValue());
							isInteger = isInteger(afiliado.getPais());
							if(!isInteger){
								LOG.info(counterLinea + " - " + "País: " + afiliado.getPais());
							}else{
								LOG.info(counterLinea + " - " + "El País no puede contener valores númericos");
								log = counterLinea + " - " + "El País no puede contener valores númericos";
								isValidAfiliado = false;
							}
						}
						break;
					case 10:
						if(campo.getValue().length() == 0){
							afiliado.setCurp(null);
							LOG.info(counterLinea + " - " + "Curp: " + afiliado.getCurp());
						}else if(campo.getValue().length() != 18){
							LOG.info(counterLinea + " - " + "El curp no cuenta con la longitud correcta");
							log = counterLinea + " - " + "El curp no cuenta con la longitud correcta";
							isValidAfiliado = false;
						}else{
							afiliado.setCurp(campo.getValue());
							LOG.info(counterLinea + " - " + "Curp: " + afiliado.getCurp());
						}
						break;
					case 11:
						if(campo.getValue().length() == 0){
							afiliado.setNss(null);
							LOG.info(counterLinea + " - " + "Nss: " + afiliado.getNss());
						}else if(campo.getValue().length() != 11){
							LOG.info(counterLinea + " - " + "El nss no cuenta con la longitud correcta");
							log = counterLinea + " - " + "El nss no cuenta con la longitud correcta";
							isValidAfiliado = false;
						}else{
							afiliado.setNss(Long.parseLong(campo.getValue()));
							LOG.info(counterLinea + " - " + "NSS: " + afiliado.getNss());
						}
						break;
					case 12:
						if(campo.getValue().length() == 0){
							LocalDate fechaNac = afiliado.getFechaNacimiento().toInstant()
									.atZone(ZoneId.systemDefault())
									.toLocalDate();

							rfc = new Rfc.Builder()
									.name(afiliado.getNombre())
									.firstLastName(afiliado.getApellidoPaterno())
									.secondLastName(afiliado.getApellidoMaterno())
									.birthday(fechaNac.getDayOfMonth(), fechaNac.getMonthValue(), fechaNac.getYear())
									.build();

							afiliado.setRfc(rfc.toString());

							if(!isVigor) {

								Afiliado bAfiliado = afiliadoService.getAfiliadoByRfc(afiliado.getRfc());
								if (bAfiliado != null) {
									LOG.info(counterLinea + " - " + "El afiliado ya se encuentra registrado");
									log = counterLinea + " - " + "El afiliado ya se encuentra registrado";

									isValidAfiliado = false;
								}
							}else{
								Afiliado bAfiliado = afiliadoService.getAfiliadoByRfc(afiliado.getRfc());
								if (bAfiliado != null) {
									LOG.info(counterLinea + " - " + "El afiliado ya se encuentra registrado");
									log = counterLinea + " - " + "El afiliado ya se encuentra registrado";

									afiliadoService.updateEstatusAfiliadoById(bAfiliado.getId());

									isValidAfiliado = false;
								}
							}

								LOG.info(counterLinea + " - " + "RFC: " + afiliado.getRfc());
						}else if(campo.getValue().length() != 13){
							LOG.info(counterLinea + " - " + "El RFC no cuenta con la longitud correcta");
							log = counterLinea + " - " + "El RFC no cuenta con la longitud correcta";
							isValidAfiliado = false;
						}else{
							afiliado.setRfc(campo.getValue());
							LOG.info(counterLinea + " - " + "RFC: " + afiliado.getRfc());
						}
						break;
					case 13:
						if(campo.getValue().length() == 0){
							afiliado.setTelefonoFijo(null);
							LOG.info(counterLinea + " - " + "Telefono fijo: " + afiliado.getTelefonoFijo());
						}else {
							afiliado.setTelefonoFijo(Long.parseLong(campo.getValue()));
							LOG.info(counterLinea + " - " + "Telefono fijo: " + afiliado.getTelefonoFijo());
						}
						break;
					case 14:
						if(campo.getValue().length() == 0){
							afiliado.setTelefonoMovil(null);
							LOG.info(counterLinea + " - " + "Telefono móvil: " + afiliado.getTelefonoMovil());
						}else {
							afiliado.setTelefonoMovil(Long.parseLong(campo.getValue()));
							LOG.info(counterLinea + " - " + "Telefono móvil: " + afiliado.getTelefonoMovil());
						}
						break;
					case 15:
						if(campo.getValue().length() == 0){
							afiliado.setEmail(null);
							LOG.info(counterLinea + " - " + "Email: " + afiliado.getEmail());
						}else{
							isValid = isValidEmail(campo.getValue());

							if(isValid){
								afiliado.setEmail(campo.getValue());
								LOG.info(counterLinea + " - " + "Email: " + afiliado.getEmail());
							}else{
								LOG.info(counterLinea + " - " + "Email inválido");
								log = counterLinea + " - " + "Email inválido";
								isValidAfiliado = false;
							}
						}
						break;
					case 16:
						afiliado.setDireccion(campo.getValue());
						LOG.info(counterLinea + " - " + "Dirección: " + afiliado.getDireccion());
						break;
					case 17:
						afiliado.setMunicipio(campo.getValue());
						LOG.info(counterLinea + " - " + "Municipio: " + afiliado.getMunicipio());
						break;
					case 18:
						if(campo.getValue().length() == 0){
							afiliado.setCodigoPostal(null);
						}else if(campo.getValue().length() != 5){
							LOG.info(counterLinea + " - " + "El código postal no cuenta con los dígitos correctos");
							log = counterLinea + " - " + "El código postal no cuenta con los dígitos correctos";
							isValidAfiliado = false;
						}else{
							afiliado.setCodigoPostal(Long.parseLong(campo.getValue()));
							LOG.info(counterLinea + " - " + "Código Postal: " + afiliado.getCodigoPostal());
						}
						break;
					case 19:
						if(campo.getValue().length() == 0){
							afiliado.setEntidadFederativa(null);
							LOG.info(counterLinea + " - " + "Entidad Federativa: " + afiliado.getEntidadFederativa());
						}else{
							isValid = isValidEntidadFederativa(campo.getValue());

							if(isValid){
								afiliado.setEntidadFederativa(campo.getValue());
								LOG.info(counterLinea + " - " + "Entidad Federativa: " + afiliado.getEntidadFederativa());
							}else{
								LOG.info(counterLinea + " - " + "La entidad federativa " + campo.getValue() + " no existe");
								log = counterLinea + " - " + "La entidad federativa " + campo.getValue() + " no existe";
								isValidAfiliado = false;
							}
						}
						break;
					case 20:
						if(campo.getValue().length() == 0){
							afiliado.setNumeroInfonavit(null);
						}else if(campo.getValue().length() != 11){
							LOG.info(counterLinea + " - " + "El número de infonavit no cumple con los dígitos totales");
							log = counterLinea + " - " + "El número de infonavit no cumple con los dígitos totales";
							isValidAfiliado = false;
						}else{
							afiliado.setNumeroInfonavit(Long.parseLong(campo.getValue()));
							LOG.info(counterLinea + " - " + "Número Infonavit: " + afiliado.getNumeroInfonavit());
						}
						break;
					case 21:
						if(campo.getValue().length() == 0){
							afiliado.setFechaAfiliacion(null);
						}else{
							isValid = isValidFormat("dd/MM/yyyy", campo.getValue());

							if(isValid){
								afiliado.setFechaAfiliacion(new SimpleDateFormat("dd/MM/yyyy").parse(campo.getValue()));
								LOG.info(counterLinea + " - " + "Fecha de Afiliación: " + afiliado.getFechaAfiliacion());
							}else{
								log = counterLinea + " - " + "Formato de fecha incorrecto, formato correcto: dd/mm/yyyy";
								LOG.info(counterLinea + " - " + "Formato de fecha incorrecto, formato correcto: dd/mm/yyyy");
								isValidAfiliado = false;
							}
						}
						break;
					case 22:
						if(campo.getValue().length() == 0){
							LOG.info(counterLinea + " - " + "El servicio no puede quedar vacío");
							log = counterLinea + " - " + "El servicio no puede quedar vacío";
							isValidAfiliado = false;
						}else{
							Servicio servicio = getServicioByNombre(campo.getValue());

							if(servicio != null){
								afiliado.setServicio(servicio);
								LOG.info(counterLinea + " - " + "Servicio: " + afiliado.getServicio().getNombre());
							}else{
								LOG.info(counterLinea + " - " + "El servicio " + campo.getValue() + " no existe en el sistema");
								log = counterLinea + " - " + "El servicio " + campo.getValue() + " no existe en el sistema";
								isValidAfiliado = false;
							}
						}
						break;
					case 23:
						if(campo.getValue().length() == 0){
							LOG.info(counterLinea + " - " + "El periodo para el servicios no puede quedar vacío");
							log = counterLinea + " - " + "El periodo para el servicios no puede quedar vacío";
							isValidAfiliado = false;
						}else{
							Periodicidad periodo = getPeriodoByNombre(campo.getValue());

							if(periodo != null){
								afiliado.setPeriodicidad(periodo);
								LOG.info(counterLinea + " - " + "Periodo: " + afiliado.getPeriodicidad().getNombre());
							}else{
								LOG.info(counterLinea + " - " + "El periodo " + campo.getValue() + " no existe en el sistema");
								log = counterLinea + " - " + "El periodo " + campo.getValue() + " no existe en el sistema";
								isValidAfiliado = false;
							}
						}
						break;
					case 24:
						afiliado.setComentarios(campo.getValue());
						LOG.info(counterLinea + " - " + "Comentarios: " + afiliado.getComentarios());
						break;
					case 25:
						if(campo.getValue().length() == 0) {
							isBeneficiario = "No";
						}else{
							isBeneficiario = campo.getValue();
						}
						break;
					case 26:
						rfcAfiliado = campo.getValue();
						break;
					case 27:
						if(campo.getValue().length() > 0) {
							Promotor promotor = getPromotorByNombre(campo.getValue());

							if(promotor != null){
								afiliado.setPromotor(promotor);
								LOG.info(counterLinea + " - " + "Promotor: " + afiliado.getPromotor().getNombre());
							}else{
								LOG.info(counterLinea + " - " + "El promotor " + campo.getValue() + " no existe en el sistema");
								log = counterLinea + " - " + "El promotor " + campo.getValue() + " no existe en el sistema";
								isValidAfiliado = false;
							}
						}
						break;
					case 28:
						if(campo.getValue().length() > 0){
							Cuenta cuenta = getCuentaByNombre(campo.getValue());

							if(cuenta != null){
								afiliado.setCuenta(cuenta);
								LOG.info(counterLinea + " - " + "Cuenta: " + afiliado.getCuenta().getRazonSocial());
							}else{
								LOG.info(counterLinea + " - " + "La cuenta " + campo.getValue() + " no existe en el sistema");
								log = counterLinea + " - " + "La cuenta " + campo.getValue() + " no existe en el sistema";
								isValidAfiliado = false;
							}
						}
						break;
					case 29:
						if(campo.getValue().length() > 0){
							corte = Integer.parseInt(campo.getValue());
						}
						break;
				}

				if(isValidAfiliado){
					continue;
				}else{
					break;
				}

			}catch(IllegalArgumentException e){
				LOG.error(counterLinea + " - " + "Error al momento de leer el archivo", e);
				log = counterLinea + " - " + "Error al momento de leer el archivo" + e.getMessage();
			}catch (ParseException pe){
				LOG.error(counterLinea + " - " + "Error al momento de convertir la fecha: ", pe);
				log = counterLinea + " - " + "Error al momento de convertir la fecha: " + pe.getMessage();
			}catch(Exception e){
				LOG.error(counterLinea + " - " + "Error al momento de leer el archivo", e);
				log = counterLinea + " - " + "Error al momento de leer el archivo" + e.getMessage();
			}
		}

		if(!isVigor){
			log = insertAfiliados(isValidAfiliado, afiliado, counterLinea);
		}else{
			log = insertAfiliadosVigor(isValidAfiliado, afiliado, counterLinea, idCuentaComercial);
		}

		return log;
	}

	/**
	 * Método que inserta los afiliados que no son vigor
	 * @param isValidAfiliado
	 * @param afiliado
	 * @param counterLinea
	 * @return
	 */

	private String insertAfiliados(boolean isValidAfiliado, Afiliado afiliado,
								   Integer counterLinea){

		try {

			if(isValidAfiliado) {

				if (afiliado.getServicio() == null || afiliado.getPeriodicidad() == null) {
					LOG.info(counterLinea + " - " + "Por favor ingrese un tipo de servicio y periodo para el Afiliado");
					log = counterLinea + " - " + "Por favor ingrese un tipo de servicio y periodo para el Afiliado";

					isValidAfiliado = false;
				} else {
					collator.setStrength(Collator.PRIMARY);
					if (isBeneficiario.equals("Sí")) {
						afiliado.setIsBeneficiario(true);
						afiliado.setEstatus(1);
						afiliado.setClave(getClaveAfiliado());
						afiliado.setFechaAlta(new Date());
						titular = afiliadoService.getAfiliadoByRfc(rfcAfiliado);

						if(titular != null){

							Double saldoAcumuladoTitular = titular.getSaldoAcumulado();
							Double saldoAcumuladoBeneficiario = afiliado.getServicio().getCostoBeneficiario() +
									afiliado.getServicio().getInscripcionBeneficiario();

							saldoAcumuladoTitular = saldoAcumuladoTitular + saldoAcumuladoBeneficiario;

							titular.setSaldoAcumulado(saldoAcumuladoTitular);

							afiliado.setServicio(titular.getServicio());
							afiliado.setCuenta(titular.getCuenta());
							afiliado.setFechaCorte(titular.getFechaCorte());
							afiliado.setPeriodicidad(titular.getPeriodicidad());

							afiliadoService.save(afiliado);
							afiliadoService.save(titular);
							afiliadoService.insertBeneficiarioUsingJpa(afiliado, titular.getId());
						}else{
							log = counterLinea + " - " + "El beneficiario no se ha insertado ya que el Titular no se ha encontrado" +
									" en el sistema";
							LOG.info(counterLinea + " - " +"El beneficiario no se ha insertado ya que el Titular no se ha encontrado" +
									" en el sistema");

							isValidAfiliado = false;
						}


					} else if (isBeneficiario.equals("No")) {
						afiliado.setIsBeneficiario(false);
						afiliado.setEstatus(1);
						afiliado.setClave(getClaveAfiliado());
						afiliado.setFechaAlta(new Date());

						if (corte > 0) {
							Date fechaCorte = calcularFechas.calcularFechas(afiliado.getPeriodicidad(), corte);
							afiliado.setFechaCorte(fechaCorte);
						}

						Double saldoAcumuladoTitular = afiliado.getServicio().getCostoTitular() +
								afiliado.getServicio().getInscripcionTitular();

						afiliado.setSaldoAcumulado(saldoAcumuladoTitular);
						afiliado.setSaldoCorte(new Double(0.00));

						afiliadoService.save(afiliado);
					}
				}

				if(!isValidAfiliado){
					LOG.info(counterLinea + " - " + "El afiliado no se ha registrado, verifique los datos que sean correctos");
				}else {
					LOG.info(counterLinea + " - " + "El afiliado se ha insertado correctamente");
					log = counterLinea + " - " + "El afiliado se ha insertado correctamente";
				}

			}else{
				LOG.info(counterLinea + " - " + "El afiliado no se ha registrado, verifique los datos que sean correctos");
			}
		}catch (Exception e){
			LOG.info(counterLinea + " - " + "Error al momento de guardar el Afiliado", e);
			log = counterLinea + " - " + "Error al momento de guardar el Afiliado: " + e.getMessage();
		}

		return log;
	}

	/**
	 * Inserta Afiliados del archivo Vigor
	 * @param isValidAfiliado
	 * @param afiliado
	 * @param counterLinea
	 * @return
	 */

	private String insertAfiliadosVigor(boolean isValidAfiliado, Afiliado afiliado,
								   Integer counterLinea, Long idCuentaComercial){
		try {

			if(isValidAfiliado) {

				if (afiliado.getServicio() == null || afiliado.getPeriodicidad() == null) {
					LOG.info(counterLinea + " - " + "Por favor ingrese un tipo de servicio y periodo para el Afiliado");
					log = counterLinea + " - " + "Por favor ingrese un tipo de servicio y periodo para el Afiliado";

					isValidAfiliado = false;
				} else {
					collator.setStrength(Collator.PRIMARY);
					if (isBeneficiario.equals("Sí")) {
						afiliado.setIsBeneficiario(true);
						afiliado.setEstatus(1);
						afiliado.setClave(getClaveAfiliado());
						afiliado.setFechaAlta(new Date());
						titular = afiliadoService.getAfiliadoByRfc(rfcAfiliado);

						if(titular != null){

							Double saldoAcumuladoTitular = titular.getSaldoAcumulado();
							Double saldoAcumuladoBeneficiario = afiliado.getServicio().getCostoBeneficiario() +
									afiliado.getServicio().getInscripcionBeneficiario();

							saldoAcumuladoTitular = saldoAcumuladoTitular + saldoAcumuladoBeneficiario;

							titular.setSaldoAcumulado(saldoAcumuladoTitular);

							afiliado.setServicio(titular.getServicio());
							afiliado.setCuenta(titular.getCuenta());
							afiliado.setFechaCorte(titular.getFechaCorte());
							afiliado.setPeriodicidad(titular.getPeriodicidad());

							afiliadoService.save(afiliado);
							afiliadoService.save(titular);
							afiliadoService.insertBeneficiarioUsingJpa(afiliado, titular.getId());
						}else{
							log = counterLinea + " - " + "El beneficiario no se ha insertado ya que el Titular no se ha encontrado" +
									" en el sistema";
							LOG.info(counterLinea + " - " +"El beneficiario no se ha insertado ya que el Titular no se ha encontrado" +
									" en el sistema");

							isValidAfiliado = false;
						}
					} else if (isBeneficiario.equals("No")) {
						afiliado.setIsBeneficiario(false);
						afiliado.setEstatus(1);
						afiliado.setClave(getClaveAfiliado());
						afiliado.setFechaAlta(new Date());

						Cuenta cuenta = cuentaService.findById(idCuentaComercial);
						afiliado.setCuenta(cuenta);

						if (corte > 0) {
							Date fechaCorte = calcularFechas.calcularFechas(afiliado.getPeriodicidad(), corte);
							afiliado.setFechaCorte(fechaCorte);
						}

						Double saldoAcumuladoTitular = afiliado.getServicio().getCostoTitular() +
								afiliado.getServicio().getInscripcionTitular();

						afiliado.setSaldoAcumulado(saldoAcumuladoTitular);
						afiliado.setSaldoCorte(new Double(0.00));

						afiliadoService.save(afiliado);
					}
				}

				if(!isValidAfiliado){
					LOG.info(counterLinea + " - " + "El afiliado no se ha registrado, verifique los datos que sean correctos");
				}else {
					LOG.info(counterLinea + " - " + "El afiliado se ha insertado correctamente");
					log = counterLinea + " - " + "El afiliado se ha insertado correctamente";
				}

			}else{
				LOG.info(counterLinea + " - " + "El afiliado no se ha registrado, verifique los datos que sean correctos");
			}
		}catch (Exception e){
			LOG.info(counterLinea + " - " + "Error al momento de guardar el Afiliado", e);
			log = counterLinea + " - " + "Error al momento de guardar el Afiliado: " + e.getLocalizedMessage();
		}

		return log;
	}

	/**
	 * Evalúa si el formato de fecha es el correcto
	 * @param format
	 * @param value
	 * @return
	 */
	private static boolean isValidFormat(String format, String value){

		boolean isValid = false;
		Date date = null;

		try{
			SimpleDateFormat dateFormat = new SimpleDateFormat(format);
			date = dateFormat.parse(value);

			if(value.equals(dateFormat.format(date))){
				isValid = true;
			}else{
				isValid = false;
			}
		}catch(ParseException pe){
			pe.printStackTrace();
		}

		return isValid;
	}

	/**
	 * Evalúa si el email está correcto
	 * @param email
	 * @return
	 */
	private boolean isValidEmail(String email){

		String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
				"[a-zA-Z0-9_+&*-]+)*@" +
				"(?:[a-zA-Z0-9-]+\\.)+[a-z" +
				"A-Z]{2,7}$";

		Pattern pattern = Pattern.compile(emailRegex);
		if(email == null){
			return false;
		}

		return pattern.matcher(email).matches();

	}

	/**
	 * Evalúa si la entidad es correcta
	 * @param entidadFederativa
	 * @return
	 */
	private boolean isValidEntidadFederativa(String entidadFederativa){

		boolean isValid = false;

		List<String> entidades = afiliadoService.getAllEstados();

		for(String entidad : entidades){
			if(entidadFederativa.equals(entidad)){
				isValid = true;
				break;
			}else{
				isValid = false;
			}
		}

		return isValid;
	}

	/**
	 * Evalúa si el servicio existe en la BBDD
	 * @param servicio
	 * @return
	 */
	private Servicio getServicioByNombre(String servicio){
		List<Servicio> listServicios = servicioService.findAll();
		Servicio nServicio = new Servicio();

		for(Servicio s : listServicios){
			if(s.getNombre().equals(servicio)){
				nServicio = s;
				break;
			}
		}

		return nServicio;
	}

	/**
	 * Evalúa si existe el periodo en la BBDD
	 * @param periodo
	 * @return
	 */
	private Periodicidad getPeriodoByNombre(String periodo){
		List<Periodicidad> listPeriodos = periodicidadService.findAll();
		Periodicidad nPeriodo = new Periodicidad();

		for(Periodicidad p : listPeriodos){
			if(p.getNombre().equals(periodo)){
				nPeriodo = p;
				break;
			}
		}

		return nPeriodo;
	}

	/**
	 * Evalúa si el promotor existe en la BBDD
	 * @param promotor
	 * @return
	 */
	private Promotor getPromotorByNombre(String promotor){
		List<Promotor> listPromotor = promotorService.findAll();
		Promotor nPromotor = new Promotor();

		for(Promotor p : listPromotor){
			if(p.getNombre().equals(promotor)){
				nPromotor = p;
				break;
			}
		}

		return nPromotor;
	}

	/**
	 * Evalúa si la cuenta existe en la BBDD
	 * @param cuenta
	 * @return
	 */
	private Cuenta getCuentaByNombre(String cuenta){
		List<Cuenta> listCuenta = cuentaService.findAll();
		Cuenta nCuenta = new Cuenta();

		for(Cuenta c : listCuenta){
			if(c.getRazonSocial().equals(cuenta)){
				nCuenta = c;
				break;
			}
		}

		return nCuenta;
	}

	/**
	 * Verifica los campos de tipo string que no sean númericos
	 * @param cadena
	 * @return
	 */
	private boolean isInteger(String cadena){
		if(NumberUtils.isCreatable(cadena)){
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Genera la clave del Afiliado
	 * @return
	 */
	private String getClaveAfiliado() {

		String claveAfiliado = "PR-";

		for (int i = 0; i < 10; i++) {
			claveAfiliado += (clave.charAt((int) (Math.random() * clave.length())));
		}

		return claveAfiliado;
	}

}