package models;

import ch.jcsinfo.system.InObject;
import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jcstritt
 */
@Entity
@Table(name = "t_etat_civil")
@Data
@EqualsAndHashCode(of = "pkEtatCivil", callSuper = false)
public class EtatCivil implements Serializable, Comparable<EtatCivil> {

  private static final long serialVersionUID = 1L;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "pkEtatCivil")
  private Integer pkEtatCivil;

  @Basic(optional = false)
  @Column(name = "abrev")
  private String abrev;

  @Basic(optional = false)
  @Column(name = "nom")
  private String nom;

  @OneToMany(mappedBy = "etatCivil", cascade = CascadeType.DETACH)
  @OrderBy("canton.abrev,nom,prenom")
  private List<Conseiller> conseillers;

  @Override
  public String toString() {
    return nom;
  }

  public String toString2() {
    return InObject.fieldsToString(this);
  }

  @Override
  public int compareTo(EtatCivil o) {
    return abrev.compareTo(o.getAbrev());
  }

}
