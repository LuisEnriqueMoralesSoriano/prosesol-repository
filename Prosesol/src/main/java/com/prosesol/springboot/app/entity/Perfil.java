package com.prosesol.springboot.app.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

@Entity
@Table(name = "perfiles")
public class Perfil implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_perfil", unique = true, nullable = false)
	private Long id;

	@Column(name = "nombre")
	private String nombre;

	@Column(name = "descripcion")
	private String descripcion;

	@Column(name = "estatus")
	private Boolean estatus;
	
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "perfiles")
	private Set<Usuario> usuarios;

	@OneToOne(mappedBy = "perfil")
	private Role roles;

	public Perfil() {
		usuarios = new HashSet<Usuario>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Boolean getEstatus() {
		return estatus;
	}

	public void setEstatus(Boolean estatus) {
		this.estatus = estatus;
	}

	public Set<Usuario> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Set<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	public Role getRoles() {
		return roles;
	}

	public void setRoles(Role roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();

		builder.append("\nId Perfil: [").append(id).append("]\n")
			   .append("Role name: [").append(nombre).append("]\n")
			   .append("Descripcion: [").append(descripcion).append("]\n");

		return builder.toString();
	}

}
