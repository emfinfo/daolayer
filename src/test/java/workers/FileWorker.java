package workers;

import beans.Conseiller;
import ch.emf.dao.Transaction;
import ch.jcsinfo.file.TextFileReader;
import java.util.List;

/**
 *
 * @author jcstritt
 */
public class FileWorker {
  DbWorkerAPI dbWrk;
  
  public FileWorker() {
    dbWrk = DbWorker.getInstance();
  }
  
  public boolean importerDonneesFichier(String fileName) {
    // idx: 0=canton 1=parti 2=conseil 3=groupe 4=fonction 5=conseiller 6=activite
    int n[] = {0, 0, 0, 0, 0, 0, 0};

    // lit le fichier CSV des fonctions de "conseiller" national suisse
    TextFileReader<Conseiller> reader = new TextFileReader<>(new ConseillerExtracter(";"));
    List<Conseiller> conseillers = reader.textFileRead(fileName, "Windows-1252");

    // transaction manuel
    Transaction tr = dbWrk.demarrerTransaction();

    // boucle sur tous les conseillers
    int i=0;
    for (Conseiller c : conseillers) {
      // System.out.println("Conseiller: "+cons+", canton: "+cons.getCanton());

      // ajouter canton
      n[0] += dbWrk.ajouterCanton(c);

      // ajouter parti
      n[1] += dbWrk.ajouterParti(c);

      // ajouter conseil
      n[2] += dbWrk.ajouterConseil(c);

      // ajouter groupe
      n[3] += dbWrk.ajouterGroupe(c);

      // ajouter fonction
      n[4] += dbWrk.ajouterFonction(c);

      // ajouter conseiller
      n[5] += dbWrk.ajouterConseiller(c);

      // ajouter activite
      n[6] += dbWrk.ajouterActivite(c);
    }

    // fin de transaction
    dbWrk.terminerTransaction(tr);
   
    // résultat
    return n[0] == 26 && n[1] > 0 && n[2] > 0 && n[3] > 0 && n[4] > 0 && n[5] > 0 && n[6] > 0;    
  }  
  
}
