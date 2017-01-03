package models;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jcstritt
 */
@Entity
@Table(name = "t_login")
@Data
@EqualsAndHashCode(of="pkLogin", callSuper=false)
public class Login implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "pkLogin")
  private int pkLogin;

  @Basic(optional = false)
  @Column(name = "nom")
  private String nom;

  @Column(name = "motDePasse")
  private String motDePasse;

  @Column(name = "domaine")
  private String domaine;

  @Column(name = "profil")
  private String profil;

  @Column(name = "email")
  private String email;

  @Column(name = "initiales")
  private String initiales;

  @Column(name = "langue")
  private String langue;

  public Login() {
  }

  @Override
  public String toString() {
    return nom + " (" + langue + ")";
  }

}
