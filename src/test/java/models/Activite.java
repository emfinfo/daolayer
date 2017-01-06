package models;

import ch.jcsinfo.datetime.DateTimeLib;
import ch.jcsinfo.system.InObject;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jcstritt
 */
@Entity
@Table(name = "t_activite")
@Data
@EqualsAndHashCode(of = "pkActivite", callSuper = false)
public class Activite implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "pkActivite")
  private Integer pkActivite;

  @Column(name = "dateEntree")
  @Temporal(TemporalType.DATE)
  private Date dateEntree;

  @Column(name = "dateSortie")
  @Temporal(TemporalType.DATE)
  private Date dateSortie;

  @JoinColumn(name = "fkConseiller", referencedColumnName = "pkConseiller")
  @ManyToOne(optional = false)
  private Conseiller conseiller ;

  @JoinColumn(name = "fkConseil", referencedColumnName = "pkConseil")
  @ManyToOne(optional = false)
  private Conseil conseil;

  @JoinColumn(name = "fkGroupe", referencedColumnName = "pkGroupe")
  @ManyToOne(optional = false)
  private Groupe groupe;

  @Override
  public String toString() {
    return conseil
      + ((groupe != null && !groupe.getAbrev().equals("?")) ? ", " + groupe : "")
      + ((dateEntree != null || dateSortie != null) ? ", (" : "")
      + ((dateEntree != null) ? DateTimeLib.dateToString(dateEntree) : "")
      + ((dateSortie != null) ? " - " + DateTimeLib.dateToString(dateSortie) : "")
      + ((dateEntree != null || dateSortie != null) ? ")" : "");
  }

  public String toString2() {
    return InObject.fieldsToString(this);
  }

}
