package com.prosesol.springboot.app.entity.rel;

import com.prosesol.springboot.app.entity.Afiliado;
import com.prosesol.springboot.app.entity.Incidencia;
import com.prosesol.springboot.app.entity.composite.id.RelAfiliadoIncidenciaId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "rel_afiliados_incidencias")
@IdClass(RelAfiliadoIncidenciaId.class)
public class RelAfiliadoIncidencia implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	@Id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_afiliado")
	private Afiliado afiliado;
	
	@Id
	@ManyToOne(fetch = FetchType.LAZY,cascade=CascadeType.ALL)
	@JoinColumn(name = "id_incidencia")
	private Incidencia incidencia;
	
	@Column(name = "fecha")
	@Temporal(TemporalType.DATE)
	private Date fecha;
	
	public Afiliado getAfiliado() {
		return afiliado;
	}

	public void setAfiliado(Afiliado afiliado) {
		this.afiliado = afiliado;
	}

	public Incidencia getIncidencia() {
		return incidencia;
	}

	public void setIncidencia(Incidencia incidencia) {
		this.incidencia = incidencia;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
}
