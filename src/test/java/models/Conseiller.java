package models;

import ch.jcsinfo.system.InObject;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 * @author jcstritt
 */
@Entity
@Table(name = "t_conseiller")

// pour appels getList avec du SQL natif
@SqlResultSetMapping(name = "ConseillerResult",
  classes = {
    @ConstructorResult(
      targetClass = Conseiller.class,
      columns = {
        @ColumnResult(name = "pkConseiller", type = Integer.class),
        @ColumnResult(name = "nom"),
        @ColumnResult(name = "prenom"),
        @ColumnResult(name = "sexe"),
        @ColumnResult(name = "origine", type = String.class),
        @ColumnResult(name = "dateNaissance", type = Date.class),
        @ColumnResult(name = "dateDeces", type = Date.class),
        @ColumnResult(name = "actif", type = Boolean.class),
        @ColumnResult(name = "fkParti"),
        @ColumnResult(name = "fkCanton"),
      }
    )
  }
)

@Data
@EqualsAndHashCode(of = "pkConseiller", callSuper = false)
public class Conseiller implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "pkConseiller")
  private int pkConseiller;

  @Basic(optional = false)
  @Column(name = "nom")
  private String nom;

  @Basic(optional = false)
  @Column(name = "prenom")
  private String prenom;

  @Basic(optional = false)
  @Column(name = "sexe")
  private String sexe;

  @Column(name = "origine")
  private String origine;

  @Column(name = "dateNaissance")
  @Temporal(TemporalType.DATE)
  private Date dateNaissance;

  @Column(name = "dateDeces")
  @Temporal(TemporalType.DATE)
  private Date dateDeces;

  @Basic(optional = false)
  @Column(name = "actif")
  private boolean actif;

  @JoinColumn(name = "fkParti", referencedColumnName = "pkParti")
  @ManyToOne(optional = false)
  private Parti parti;

  @JoinColumn(name = "fkCanton", referencedColumnName = "pkCanton")
  @ManyToOne(optional = false)
  private Canton canton;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "conseiller")
  @OrderBy("dateEntree ASC")
  private List<Activite> activites;

  // attributs juste pour la lecture du fichier .csv
  @Transient
  private Activite activite;

  @Transient
  private Conseil conseil;

  @Transient
  private Groupe groupe;

  @Transient
  private Fonction fonction;

  // attributs juste pour la lecture en SQL natif
  @Transient
  private Integer fkCantonSQL;

  @Transient
  private Integer fkPartiSQL;

  // calcul d'un clé pour identifier des conseillers uniques
  public String getKey() {
    return nom
      + " " + prenom
      //      + " " + this.origine
      + " " + sexe
      //      + " " + DateTimeLib.dateToString(this.dateNaissance);
      + " " + canton.getAbrev()
      + " " + parti.getNomParti();
  }

  public Conseiller() {
  }

  // pour les appels de getList avec du SQL natif
  public Conseiller(Integer pkConseiller, String nom, String prenom, String sexe,
      String origine, Date dateNaissance, Date dateDeces, boolean actif,
      Integer fkParti, Integer fkCanton) {
    this.pkConseiller = pkConseiller;
    this.nom = nom;
    this.prenom = prenom;
    this.sexe = sexe;
    this.origine = origine;
    this.dateNaissance = dateNaissance;
    this.dateDeces = dateDeces;
    this.actif = actif;
    this.fkPartiSQL = fkParti;
    this.fkCantonSQL = fkCanton;
  }

  @Override
  public String toString() {
    return nom + " " + prenom;
//      + " ("
//      + ((dateNaissance != null) ? DateTimeLib.dateToString(dateNaissance) : "?")
//      + ((dateDeces != null) ? " - " + DateTimeLib.dateToString(dateDeces) : "") + ")"
//      + ", actif=" + ((actif)?"OUI":"NON");
  }

  public String toString2() {
    return InObject.fieldsToString(this);
  }

}
