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
@Table(name = "t_canton")
@Data
@EqualsAndHashCode(of="pkCanton", callSuper=false)
public class Canton implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Basic(optional = false)
  @Column(name = "pkCanton")
  private int pkCanton;

  @Basic(optional = false)
  @Column(name = "abrev")
  private String abrev;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "canton")
  private List<Conseiller> conseillers;

  public Canton() {
  }

  @Override
  public String toString() {
    return abrev;
  }

}
