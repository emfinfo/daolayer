package models;

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
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jcstritt
 */
@Entity
@Table(name = "t_fonction")
@Data
@EqualsAndHashCode(of = "pkFonction", callSuper = false)
public class Fonction implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "pkFonction")
  private int pkFonction;

  @Basic(optional = false)
  @Column(name = "nomFonction")
  private String nomFonction;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "fonction")
  private List<Activite> activites;

  public Fonction() {
  }

  @Override
  public String toString() {
    return nomFonction.toLowerCase();
  }

}
