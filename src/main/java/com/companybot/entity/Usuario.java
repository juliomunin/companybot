package com.companybot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_users")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    private Long iduser;

    @Column(name = "idperson")
    private Long idperson;

    @Column(name = "deslogin")
    private String deslogin;

    @Column(name = "despassword")
    private String despassword;

    @Column(name = "inadmin")
    private Integer inadmin;

    @Column(name = "dtregister")
    private LocalDateTime dtregister;

    public Long getIduser() {
        return iduser;
    }

    public void setIduser(Long iduser) {
        this.iduser = iduser;
    }

    public Long getIdperson() {
        return idperson;
    }

    public void setIdperson(Long idperson) {
        this.idperson = idperson;
    }

    public String getDeslogin() {
        return deslogin;
    }

    public void setDeslogin(String deslogin) {
        this.deslogin = deslogin;
    }

    public String getDespassword() {
        return despassword;
    }

    public void setDespassword(String despassword) {
        this.despassword = despassword;
    }

    public Integer getInadmin() {
        return inadmin;
    }

    public void setInadmin(Integer inadmin) {
        this.inadmin = inadmin;
    }

    public LocalDateTime getDtregister() {
        return dtregister;
    }

    public void setDtregister(LocalDateTime dtregister) {
        this.dtregister = dtregister;
    }
}