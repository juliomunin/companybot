package com.companybot.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_persons")
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idperson")
    private Long idperson;

    @Column(name = "desperson")
    private String desperson;

    @Column(name = "desemail")
    private String desemail;

    @Column(name = "desnick")
    private String desnick;

    @Column(name = "nrddd")
    private Integer nrddd;

    @Column(name = "nrphone")
    private Long nrphone;

    @Column(name = "dtbirth")
    private LocalDate dtbirth;

    @Column(name = "dtregister")
    private LocalDateTime dtregister;

    public Long getIdperson() {
        return idperson;
    }

    public void setIdperson(Long idperson) {
        this.idperson = idperson;
    }

    public String getDesperson() {
        return desperson;
    }

    public void setDesperson(String desperson) {
        this.desperson = desperson;
    }

    public String getDesemail() {
        return desemail;
    }

    public void setDesemail(String desemail) {
        this.desemail = desemail;
    }

    public String getDesnick() {
        return desnick;
    }

    public void setDesnick(String desnick) {
        this.desnick = desnick;
    }

    public Integer getNrddd() {
        return nrddd;
    }

    public void setNrddd(Integer nrddd) {
        this.nrddd = nrddd;
    }

    public Long getNrphone() {
        return nrphone;
    }

    public void setNrphone(Long nrphone) {
        this.nrphone = nrphone;
    }

    public LocalDate getDtbirth() {
        return dtbirth;
    }

    public void setDtbirth(LocalDate dtbirth) {
        this.dtbirth = dtbirth;
    }

    public LocalDateTime getDtregister() {
        return dtregister;
    }

    public void setDtregister(LocalDateTime dtregister) {
        this.dtregister = dtregister;
    }
}