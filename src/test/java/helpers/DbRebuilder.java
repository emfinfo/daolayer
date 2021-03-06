package helpers;

import ch.emf.dao.JpaDaoAPI;
import ch.emf.dao.filtering.Search;
import ch.emf.dao.transactions.Transaction;
import ch.jcsinfo.file.TextFileReader;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import models.Activite;
import models.Canton;
import models.Conseil;
import models.Conseiller;
import models.EtatCivil;
import models.Groupe;
import models.Parti;

/**
 * Classe pour reconstituer la base de données des conseillers
 * d'après un fichier csv.
 *
 * @author jcstritt
 */
public class DbRebuilder {
  private JpaDaoAPI dao;

  @Inject
  public DbRebuilder(JpaDaoAPI dao) {
    this.dao = dao;
  }

  public boolean importerDonneesFichier(String fileName) {
    // idx: 0=canton 1=parti 2=conseil 3=groupe 4=fonction 5=conseiller 6=activite
    int n[] = {0, 0, 0, 0, 0, 0, 0};

    // lit le fichier CSV des fonctions de "conseiller" national suisse
    TextFileReader<Conseiller> reader = new TextFileReader<>(new ConseillerExtracter(";"));
    List<Conseiller> conseillers = reader.read(fileName, "UTF-8");

    // boucle sur tous les conseillers
    System.out.println("Reconstitution de la BD 'parlement' ...");
    System.out.println("  - nb de lignes lues: " + conseillers.size());

    // création de hashmap pour disposer d'entrées uniques triées
    System.out.println("  - création de listes simples ... ");
    Map<String, EtatCivil> mapEtatsCivils = new HashMap<>();
    Map<String, Canton> mapCantons = new HashMap<>();
    Map<String, Parti> mapPartis = new HashMap<>();
    Map<String, Conseil> mapConseils = new HashMap<>();
    Map<String, Groupe> mapGroupes = new HashMap<>();
    Map<String, Conseiller> mapConseillers = new HashMap<>();

    // on remplit les hashmap
    for (Conseiller c : conseillers) {
      mapEtatsCivils.put(c.getEtatCivil().getAbrev(), c.getEtatCivil());
      mapCantons.put(c.getCanton().getAbrev(), c.getCanton());
      mapPartis.put(c.getParti().getAbrev(), c.getParti());
      mapConseils.put(c.getConseil().getAbrev(), c.getConseil());
      mapGroupes.put(c.getGroupe().getAbrev(), c.getGroupe());

      // traitement spécial pour la haspmap des conseillers
      Conseiller c2 = mapConseillers.get(c.getKey());
      if (c2 == null) {
        mapConseillers.put(c.getKey(), c);
      } else if (c.isActif() != c2.isActif()) {
        c2.setActif(c2.isActif() || c.isActif());
        mapConseillers.replace(c.getKey(), c2);
      }

      // traitement spécial si plusieurs citoyennetés (on prend la 1ère)
      String t[] = c.getCitoyennete().split(",");
      if (t.length > 0) {
        c.setCitoyennete(t[0]);
      }
    }

    // tri par ordre alphabétique
    System.out.println("  - tri des listes simples ...");
    List<EtatCivil> etatsCivils = new ArrayList<>(mapEtatsCivils.values());
    Collections.sort(etatsCivils);
    List<Canton> cantons = new ArrayList<>(mapCantons.values());
    Collections.sort(cantons);
    List<Parti> partis = new ArrayList<>(mapPartis.values());
    Collections.sort(partis);
    List<Conseil> conseils = new ArrayList<>(mapConseils.values());
    Collections.sort(conseils);
    List<Groupe> groupes = new ArrayList<>(mapGroupes.values());
    Collections.sort(groupes);
    List<Conseiller> conseillers2 = new ArrayList<>(mapConseillers.values());
    Collections.sort(conseillers2);

    // ajout dans la BD
    System.out.println("  - ajout des listes simples dans la BD ...");
    n[0] = dao.insertList(EtatCivil.class, etatsCivils, false);
    n[1] = dao.insertList(Canton.class, cantons, false);
    n[2] = dao.insertList(Parti.class, partis, false);
    n[3] = dao.insertList(Conseil.class,conseils, false);
    n[4] = dao.insertList(Groupe.class, groupes, false);

    // maj des hashmap avec les objets de la BD
    for (EtatCivil ec : etatsCivils) {
      mapEtatsCivils.replace(ec.getAbrev(), ec);
    }
    for (Canton ct : cantons) {
      mapCantons.replace(ct.getAbrev(), ct);
    }
    for (Parti pa : partis) {
      mapPartis.replace(pa.getAbrev(), pa);
    }
    for (Conseil co : conseils) {
      mapConseils.replace(co.getAbrev(), co);
    }
    for (Groupe gr : groupes) {
      mapGroupes.replace(gr.getAbrev(), gr);
    }

    // maj des conseillers avec les bons objets récupérés des hashmap
    System.out.println("  - màj des conseillers avec les objets de la BD ...");
    for (Conseiller c : conseillers2) {
      c.setEtatCivil(mapEtatsCivils.get(c.getEtatCivil().getAbrev()));
      c.setCanton(mapCantons.get(c.getCanton().getAbrev()));
      c.setParti(mapPartis.get(c.getParti().getAbrev()));
    }

    // ajout des conseillers dans la BD et maj de la hashmap avec les objets de la BD
    System.out.println("  - ajout des conseillers dans la BD ...");
    n[5] = dao.insertList(Conseiller.class, conseillers2, false);
    for (Conseiller c2 : conseillers2) {
      mapConseillers.replace(c2.getKey(), c2);
    }

    System.out.println("  - ajout des activités dans la BD ...");
    Transaction tr = dao.getTransaction();
    tr.beginManualTransaction();
    int i = 0;
    for (Conseiller c : conseillers) {
      Activite act = c.getActivite();
      act.setConseil(mapConseils.get(c.getConseil().getAbrev()));
      act.setGroupe(mapGroupes.get(c.getGroupe().getAbrev()));
      act.setConseiller(mapConseillers.get(c.getKey()));
      n[6] += ajouterActivite(act);
    }
    try {
      tr.commitManualTransaction();
    } catch (Exception ex) {
    }
    tr.finishManualTransaction();
    System.out.println("Fin de la reconstitution de la BD ... " + n[6]);

    // résultat
    return n[0] > 0 && n[1] > 0 && n[2] > 0 && n[3] > 0 && n[4] > 0 && n[5] > 0 && n[6] > 0;
  }

  private int ajouterActivite(Activite activite) {
    Search s = new Search(Activite.class);
    s.addFilterEqual("dateEntree", activite.getDateEntree());
    s.addFilterAnd();
    s.addFilterEqual("dateSortie", activite.getDateSortie());
    s.addFilterAnd();
    s.addFilterEqual("conseiller", activite.getConseiller());
    s.addFilterAnd();
    s.addFilterEqual("conseil", activite.getConseil());
    s.addFilterAnd();
    s.addFilterEqual("groupe", activite.getGroupe());
    int n = 0;
    if (dao.getSingleResult(s) == null) {
      n = dao.create(activite);
    }
    return n;
  }

}
