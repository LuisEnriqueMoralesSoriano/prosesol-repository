package com.prosesol.springboot.app.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "logs")
public class LogCM implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log")
    private Long id;

    @Column(name = "nombre_archivo")
    private String nombre;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd/MM/yy")
    private Date fecha;

    @Column(name = "numero_registros")
    private Integer numeroRegistros;

    @Lob
    @Column(name = "archivo")
    private byte[] archivo;

    public LogCM(){};

    public LogCM(String nombre, Date fecha, Integer numeroRegistros, byte[] archivo) {
        this.nombre = nombre;
        this.fecha = fecha;
        this.numeroRegistros = numeroRegistros;
        this.archivo = archivo;
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

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Integer getNumeroRegistros() {
        return numeroRegistros;
    }

    public void setNumeroRegistros(Integer numeroRegistros) {
        this.numeroRegistros = numeroRegistros;
    }

    public byte[] getArchivo() {
        return archivo;
    }

    public void setArchivo(byte[] archivo) {
        this.archivo = archivo;
    }

    @Override
    public String toString() {
        return "LogCM{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", fecha=" + fecha +
                ", numeroRegistros=" + numeroRegistros +
                '}';
    }
}
