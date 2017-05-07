package filtre_anti_spam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class FiltreAntiSpam implements Serializable {
	
	public static final double epsilon = 1;
	
	public double[] bSpam;
	public double[] bHam;
	
	public double PSpam;
	public double PHam;

	private ArrayList<String> dico;


	public FiltreAntiSpam() {

		dico = new ArrayList<>();
		
		
	}
	
	public void chargerDico(String fichier){
		String ligne;
		try{
			System.out.println(fichier);
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			
			while ((ligne=br.readLine())!=null){
				// DEBUG
				//System.out.println(ligne);
				
				if(ligne.length() >= 3){
					dico.add(ligne);
				}
			}
			br.close(); 
		}		
		catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public boolean[] createMailVector(String fichier){
		boolean message[] = new boolean[dico.size()];
		for(int i = 0; i < message.length;i++){
			message[i] = false;
		}
		
		String ligne;
		int index;
		String sac[];
		String regex = " ?[,;:...]? | [,;:...]? ?|[,;:...]";
		try{
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			
			while ((ligne=br.readLine())!=null){
				sac = ligne.split(regex);
				for(String mot : sac){
					if(mot.length()>=3){
						mot = mot.toUpperCase();
						if(dico.contains(mot)){
							index = dico.indexOf(mot);
							message[index] = true;
						}
					}
				}
			}
			br.close(); 
		}		
		catch (Exception e){
			e.printStackTrace();
		}
		
		return message;
	}
	
	public void apprentissage(int nbHam,int nbSpam,String fichier) {
		chargerDico(fichier);
		//TODO GET VARIABLE
		int dicoSize = dico.size();
		int nbMail =nbSpam + nbHam;
		String SpamDirectory = "baseapp/spam";
		String HamDirectory = "baseapp/ham";
		
		System.out.println();
		System.out.println();
		System.out.print("Apprentissage");
		
		int[] apparitionMotsSpam;
		int[] apparitionMotsHam;
		try {
			apparitionMotsSpam = apprentissageOccurrenceMotsMail(SpamDirectory, nbSpam);
			System.out.print(".");
			apparitionMotsHam = apprentissageOccurrenceMotsMail(HamDirectory, nbHam); 
			System.out.print(".");
			
			this.bSpam = new double[dicoSize];
			this.bHam = new double[dicoSize];
			
			
			//SPAM, estimation des probabilites par les frequences
			for(int i=0; i<dicoSize; i++){
				this.bSpam[i] = (double) ( (double) (apparitionMotsSpam[i] + epsilon) / (double) (nbSpam + 2*epsilon) );
			}
			
			//HAM, estimation des probabilites par les frequences
			for(int i=0; i<dicoSize; i++){
				this.bHam[i] = (double) ( (double) (apparitionMotsHam[i] + epsilon) / (double) (nbHam + 2*epsilon) );
			}
			
			System.out.print(".");
			
			//SPAM, estimation Probabilite a posteriori P(Y = SPAM)
			this.PSpam = (double) ( (double) nbSpam / (double) nbMail) ;
			
			//HAM, estimation Probabilite a posteriori P(Y = HAM)
			this.PHam = 1 - this.PSpam;
			
			//System.out.println("PHam : "+PHam+"PSpam : "+PSpam);
			System.out.println("    FAIT");
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	private void apprentissage(String mail, int spam) {
		
	}
	
	public int[] apprentissageOccurrenceMotsMail(String directoryName, int endIndex) throws Exception {
		int mots[] = new int[dico.size()];
		for(int i = 0; i < mots.length;i++){
			mots[i] = 0;
		}
		boolean vecteur[];
		String [] files;
		File repertoire = new File(directoryName);
		files= repertoire.list();
		
		if(endIndex > files.length){
			throw new Exception("taille de la base d apprentissage: " + directoryName + " invalide");
		}
		
		for(int j=0; j<endIndex; j++){
			vecteur = createMailVector(directoryName + "/" + files[j]);
			for(int i = 0; i < vecteur.length;i++){
				if(vecteur[i]){
					mots[i]++;
				}
			}
		}

		return mots;
	}
	
	public boolean verifyMail(String path) throws Exception {
		//read file and get binary vector x
		boolean[] x = this.createMailVector(path);
		
		int dicoSize = dico.size();
		double PMailSpam = 0.;
		double PMailHam = 0.;
		
		
		boolean j;
		//SPAM
		for(int i=0; i<dicoSize; i++){
			j = x[i];
			if(j == true){
				PMailSpam += this.bSpam[i] > 0 ? Math.log(this.bSpam[i]) : 0;
			} else if(j == false){
				PMailSpam += 1 - this.bSpam[i] > 0 ? Math.log(1 - this.bSpam[i]) : 0;
			} else {
				throw new Exception("Critical Error");
			}
		}
		PMailSpam += this.PSpam > 0 ? Math.log(this.PSpam) : 0;
		
		//HAM
		for(int i=0; i<dicoSize; i++){
			j = x[i];
			if(j == true){
				PMailHam += this.bHam[i] > 0 ? Math.log(this.bHam[i]) : 0;
			} else if(j == false){
				PMailHam += 1 - this.bHam[i] > 0 ? Math.log(1 - this.bHam[i]) : 0;
			} else {
				throw new Exception("Critical Error");
			}
		}
		PMailHam += this.PHam > 0 ? Math.log(this.PHam) : 0;
		
		
		
		double pSpam = 1.0 / (1.0 + Math.exp(PMailHam - PMailSpam));
		double pHam = 1.0 / (1.0 + Math.exp(PMailSpam - PMailHam));
		//double px = Math.exp(PMailSpam+PMailHam);
		double px = pHam+pSpam;
		System.out.println(": P(Y=SPAM | X=x) =" + pSpam + ", P(Y=HAM | X=x) =" + pHam);
		System.out.print("              =>");
		
		// Estimation SPAM ou HAM

		
		double res = Math.max(pSpam, pHam);
		if(res == pHam){
			// mail considere comme un HAM
			return false;
		} else if(res == pSpam){
			// mail considere comme un SPAM
			return true;
		} else {
			throw new Exception("Critical Error");
		}
		//TODO affichage
	}
	
	public void test(String directoryPath) {
		String [] files;
		File repertoire;
		boolean res;
		
		System.out.println();
		System.out.println("Test:");
		System.out.println();
		
		int spamError = 0;
		int hamError = 0;
		int spamSize=0;
		int	hamSize = 0;
		//TEST SPAM
		repertoire = new File(directoryPath + "/spam");
		files= repertoire.list();
		for(String fileName : files){
			spamSize++;
			System.out.print("SPAM " + fileName);
			try {
				res = this.verifyMail(directoryPath + "/spam/" + fileName);
				if(res == true){
					System.out.print("identifie comme un SPAM");
					System.out.println();
				} else {
					System.out.print("identifie comme un HAM  ***Erreur***");
					System.out.println();
					spamError++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//TEST HAM
		repertoire = new File(directoryPath + "/ham");
		files= repertoire.list();
		for(String fileName : files){
			hamSize++;
			System.out.print("HAM " + fileName);
			try {
				res = this.verifyMail(directoryPath + "/ham/" + fileName);
				if(res == true){
					System.out.print(" identifie comme un SPAM  ***Erreur***");
					System.out.println();
					hamError++;
				} else {
					System.out.print(" identifie comme un HAM");
					System.out.println();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(spamError+" erreurs de spam sur "+spamSize+" spams, pourcentage d'erreur : "+ ((float)spamError/(float)spamSize*100) + "%");
		System.out.println(hamError+" erreurs de ham sur "+hamSize+" hams, pourcentage d'erreur : "+ ((float)hamError/(float)hamSize*100) + "%");
		
	}
	
	public void save(String fileName){
		ObjectOutputStream oos = null;
		
		try {
		      final FileOutputStream fichier = new FileOutputStream(fileName);
		      oos = new ObjectOutputStream(fichier);
		      oos.writeObject(this);
		      oos.flush();
		    } catch (final java.io.IOException e) {
		      e.printStackTrace();
		    } finally {
		      try {
		        if (oos != null) {
		          oos.flush();
		          oos.close();
		        }
		      } catch (final IOException ex) {
		        ex.printStackTrace();
		      }
		    }
	}
	
	public FiltreAntiSpam load(String fileName){
		ObjectInputStream ois = null;
		FiltreAntiSpam fas = null;
		  FileInputStream fichier;
		try {
			fichier = new FileInputStream(fileName);
			ois = new ObjectInputStream(fichier);
		    fas = (FiltreAntiSpam) ois.readObject();
		    ois.close();
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}
		return fas;
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length == 4){
			//sauvegarde du classifieur
			String classifieur = args[0];
			String baseApp = args[1];
			int nbHam = Integer.parseInt(args[2]); 
			int nbSpam = Integer.parseInt(args[3]); 
			FiltreAntiSpam fas = new FiltreAntiSpam();
			fas.apprentissage(nbHam,nbSpam,"dictionnaire1000en.txt");
			fas.save(classifieur);
			System.out.println("Classifieur enregistré dans ’"+classifieur +"’.");
		}else if (args.length == 3){
			String classifieur = args[0];
			String mail = args[1];
			int spam = -1;
			if(args[3].equals("SPAM")){
				spam = 0;
			}else if(args[3].equals("HAM")){
				spam = 1;
			}else{
				System.out.println("ERREUR SAISI");
			}
			
			FiltreAntiSpam fas = new FiltreAntiSpam();
			fas = fas.load(classifieur);

			fas.apprentissage(mail,spam);
			
			fas.save(classifieur);
		}else if (args.length == 2){
			//chargement du classifieur et execution
			String classifieur = args[0];
			String mail = args[1];
			FiltreAntiSpam fas = new FiltreAntiSpam();
			fas = fas.load(classifieur);
			try {
				
				if(fas.verifyMail(mail)){
					System.out.println("D’après ’"+classifieur+"’, le message ’"+mail+"’ est un SPAM !");
				}else{
					System.out.println("D’après ’"+classifieur+"’, le message ’"+mail+"’ est un HAM !");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else{
			//execution standard
			FiltreAntiSpam fas = new FiltreAntiSpam();
	
			fas.apprentissage(100,200,"dictionnaire1000en.txt");
			
			fas.test("basetest");
		}
	}

	

	

	
}
