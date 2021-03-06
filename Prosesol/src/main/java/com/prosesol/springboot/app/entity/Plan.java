package com.prosesol.springboot.app.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Luis Enrique Morales Soriano
 */
@Entity
@Table(name = "planes_openpay")
public class Plan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id_plan")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "id_servicio", referencedColumnName = "id_servicio")
    private Servicio servicio;

    @Column(name = "nombre_servicio")
    private String nombreServicio;

    @Column(name = "id_plan_openpay")
    private String planOpenpay;

    public Plan(){}

    public Plan(Servicio servicio, String nombreServicio, String planOpenpay) {
        this.servicio = servicio;
        this.nombreServicio = nombreServicio;
        this.planOpenpay = planOpenpay;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Servicio getServicio() {
        return servicio;
    }

    public void setServicio(Servicio servicio) {
        this.servicio = servicio;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public String getPlanOpenpay() {
        return planOpenpay;
    }

    public void setPlanOpenpay(String planOpenpay) {
        this.planOpenpay = planOpenpay;
    }
}
