package workers;

import ch.jcsinfo.datetime.DateTimeLib;
import ch.jcsinfo.file.BeanExtracter;
import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.EtatCivil;
import models.Groupe;
import models.Parti;

/**
 * Extracteur d'un bean "Conseiller" depuis une ligne de fichier csv.
 *
 * @author Jean-Claude Stritt
 */
public class ConseillerExtracter implements BeanExtracter<Conseiller> {
  private final String INDEFINI_ABREV = "?";
  private final String INDEFINI_NOM = "--";

  private String sep;

  public ConseillerExtracter(String sep) {
    this.sep = sep;
  }

//  public String deAccent(String str) {
//    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
//    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//    return pattern.matcher(nfdNormalizedString).replaceAll("");
//  }

  @Override
  public Conseiller textToBean(int idx, String line) {
    String[] tab = line.split(sep);
    Conseiller cons = new Conseiller();

    if (tab.length >= 19) {

      // conseiller
      cons.setActif(tab[0].trim().equals("VRAI"));
      cons.setPrenom(tab[1].trim());
      cons.setNom(tab[2].trim());
      cons.setSexe(tab[3].trim());
      cons.setLieuNaissance(tab[12].trim());
      cons.setCantonNaissance(tab[13].trim());
      cons.setMandats(tab[14].trim());
      cons.setCitoyennete(tab[17].trim());
      cons.setDateNaissance(DateTimeLib.parseIsoDate(tab[18]));
      if (tab.length >= 20) {
        cons.setDateDeces(DateTimeLib.parseIsoDate(tab[19]));
      }

      // canton
      Canton canton = new Canton();
      canton.setNom(tab[4].trim());
      canton.setAbrev(tab[5].trim());
      if (canton.getAbrev().isEmpty()) {
        canton.setAbrev(INDEFINI_ABREV);
        canton.setNom(INDEFINI_NOM);
      }
      cons.setCanton(canton);

      // conseil
      Conseil conseil = new Conseil();
      conseil.setNom(tab[6].trim());
      String s = conseil.getNom().toLowerCase();
      if (s.contains("nal")) {
        conseil.setAbrev("CN");
      } else if (s.contains("ats")) {
        conseil.setAbrev("CE");
      } else if (s.contains("ral")) {
        conseil.setAbrev("CF");
      } else {
        conseil.setAbrev(INDEFINI_ABREV);
        conseil.setNom(INDEFINI_NOM);
      }
      cons.setConseil(conseil);

      // groupe
      Groupe groupe = new Groupe();
      groupe.setNom(tab[7].trim());
      groupe.setAbrev(tab[8].trim());
      if (groupe.getAbrev().isEmpty()) {
        groupe.setAbrev(INDEFINI_ABREV);
        groupe.setNom("Inconnu");
      } else {
        groupe.setAbrev(groupe.getAbrev().toUpperCase());
      }
      cons.setGroupe(groupe);

      // parti
      Parti parti = new Parti();
      parti.setNom(tab[9].trim());
      parti.setAbrev(tab[10].trim());
      if (parti.getAbrev().isEmpty()) {
        parti.setAbrev(INDEFINI_ABREV);
        parti.setNom("Indéfinissable");
      } else {
        parti.setNom(parti.getNom().substring(0, 1).toUpperCase() + parti.getNom().substring(1));
        if (parti.getAbrev().substring(0, 1).compareTo("Z") > 0) {
          if (!parti.getAbrev().contains("proc ")) {
            parti.setAbrev(parti.getAbrev().toUpperCase());
          }
        }
      }
      cons.setParti(parti);

      // etat civil
      EtatCivil ec = new EtatCivil();
      ec.setNom(tab[11].trim());
      if (ec.getNom().isEmpty()) {
        ec.setAbrev(INDEFINI_ABREV);
        ec.setNom(INDEFINI_NOM);
      } else {
        ec.setAbrev(ec.getNom().substring(0, 1).toUpperCase());
      }
      cons.setEtatCivil(ec);

      // activite
      Activite activite = new Activite();
      activite.setDateEntree(DateTimeLib.parseIsoDate(tab[15]));
      activite.setDateSortie(DateTimeLib.parseIsoDate(tab[16]));
      cons.setActivite(activite);

//      System.out.println(cons.toString2());

    } else {
      System.out.println(idx+". " + line);
    }
    return cons;
  }

}
