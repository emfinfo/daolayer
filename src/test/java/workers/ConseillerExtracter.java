package workers;

import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.Fonction;
import models.Groupe;
import models.Parti;
import ch.jcsinfo.datetime.DateTimeLib;
import ch.jcsinfo.file.TextFileExtracter;

/**
 * Extracteur de ligne csv (ou autre) pour les conseillers. Exemple :<br>
 * 0    1        2       3 4  5  6      7      8             9         10<br>
 * FAUX;Giuseppe;a Marca;m;GR;CE;Centre;Membre;Conservateurs;1851-07-2;1849-12-2<br>
 * 
 * @author Jean-Claude Stritt
 */
public class ConseillerExtracter implements TextFileExtracter<Conseiller> {
  private final String INDEFINISSABLE = "Indéfinissable";
  private final String INCONNU = "Inconnu";
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
  public Conseiller extract(String line, int lineNbr) {
    String[] tab = line.split(sep);
    Conseiller cons = new Conseiller();
    String abrev;
    
    if (tab.length >= 11) {
      // conseiller
      cons.setActif(tab[0].trim().equals("VRAI"));
      cons.setPrenom(tab[1]);
      cons.setNom(tab[2]);
      cons.setSexe(tab[3]);
//      conseiller.setOrigine(tab[?].trim()); // TODO: à remplacer dès info connue
//      conseiller.setDateNaissance(DateTimeLib.stringToDate(tab[?])); // TODO: à remplacer dès info connue
//      conseiller.setDateDeces(DateTimeLib.stringToDate(tab[?])); // TODO: à remplacer dès info connue
      cons.setOrigine(null);
      cons.setDateNaissance(null);
      cons.setDateDeces(null);

      // canton
      Canton canton = new Canton();
      canton.setAbrev(tab[4]);
      cons.setCanton(canton);

      // conseil
      Conseil conseil = new Conseil();
      conseil.setAbrev(tab[5]);
      cons.setConseil(conseil);

      // groupe
      Groupe groupe = new Groupe();
      groupe.setNomGroupe(tab[6].trim());
      if (groupe.getNomGroupe().length() < 2) {
        groupe = null;
      }
      cons.setGroupe(groupe);

      // fonction
      Fonction fonction = new Fonction();
      fonction.setNomFonction(tab[7].trim());
      if (cons.getGroupe()==null || fonction.getNomFonction().length() < 2) {
        fonction = null;
      }
      cons.setFonction(fonction);

      // parti
      Parti parti = new Parti();
      parti.setNomParti(tab[8].trim());
      if (parti.getNomParti().length() < 2) {
        parti.setNomParti(INDEFINISSABLE);
      }
      parti.setNomParti(parti.getNomParti().substring(0, 1).toUpperCase() + parti.getNomParti().substring(1));
      cons.setParti(parti);
      
      // activite
      Activite activite = new Activite();
      activite.setDateSortie(DateTimeLib.isoStringToDate(tab[9]));
      activite.setDateEntree(DateTimeLib.isoStringToDate(tab[10]));
      cons.setActivite(activite);
      
    }
    return cons;
  }

}
