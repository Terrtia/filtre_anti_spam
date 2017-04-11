package filtre_anti_spam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FiltreAntiSpam {
	
	public static final int epsilon = 1;
	
	public double[] bSpam;
	public double[] bHam;
	
	public double PSpam;
	public double PHam;

	private ArrayList<String> dico;


	public FiltreAntiSpam(String fichier) {

		dico = new ArrayList<>();
		chargerDico(fichier);
		
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
	
	public void apprentissage() {
		
		//TODO GET VARIABLE
		int dicoSize = dico.size();
		int nbSpam = 500;
		int nbHam = 500;
		int nbMail = nbSpam + nbHam;
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
			for(int i=0; i<nbSpam; i++){
				this.bSpam[i] = (double) ( (double) (apparitionMotsSpam[i] + epsilon) / (double) (nbSpam + 2*epsilon) );
			}
			
			//HAM, estimation des probabilites par les frequences
			for(int i=0; i<nbHam; i++){
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
		System.out.println(x.length);
		
		int dicoSize = dico.size();
		double PMailSpam = 0;
		double PMailHam = 0;
		
		boolean j;
		//SPAM
		for(int i=0; i<dicoSize; i++){
			j = x[i];
			if(j == true){
				PMailSpam = Math.log(this.bSpam[i]) + PMailSpam;
			} else if(j == false){
				PMailSpam = Math.log(1 - this.bSpam[i]) + PMailSpam;
			} else {
				throw new Exception("Critical Error");
			}
		}
		System.out.println("PSpam : "+PSpam);
		System.out.println("log(PSpam) : "+Math.log(PSpam));
		System.out.println("PMailSpam : "+PMailSpam);
		PMailSpam = Math.log(this.PSpam) + PMailSpam;
		System.out.println("PMailSpam : "+PMailSpam);
		
		//HAM
		for(int i=0; i<dicoSize; i++){
			j = x[i];
			if(j == true){
				PMailHam = Math.log(this.bHam[i]) + PMailHam;
			} else if(j == false){
				PMailHam = Math.log(1 - this.bHam[i]) + PMailHam;
			} else {
				throw new Exception("Critical Error");
			}
		}
		PMailHam = Math.log(this.PHam) + PMailHam;
		
		// Estimation SPAM ou HAM
		double res = Math.max(PMailSpam, PMailHam);
		if(res == PMailSpam){
			// mail considere comme un SPAM
			return true;
		} else if(res == PMailHam){
			// mail considere comme un HAM
			return false;
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
					System.out.print("identifie comme un SPAM  ***Erreur***");
					System.out.println();
					hamError++;
				} else {
					System.out.print("identifie comme un HAM");
					System.out.println();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(spamError+" erreurs de spam "+spamSize+" spams, pourcentage d'erreur : "+ (spamError/spamSize*100) + "%");
		System.out.println(hamError+" erreurs de ham "+hamSize+" hams, pourcentage d'erreur : "+ (hamError/hamSize*100) + "%");
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FiltreAntiSpam fas = new FiltreAntiSpam("dictionnaire1000en.txt");

		fas.apprentissage();
		
		// DEBUG
		/*for(int i=0; i<1000; i++){
			System.out.println("bjSPam= " + fas.bSpam[i] + " | bjHam= " + fas.bHam[i]);
		}
		System.out.println();
		System.out.println("P(Y=SPAM)= " + fas.PSpam + " | P(Y=HAM)= " + fas.PHam);*/
		
		fas.test("basetest");
		
	}

}
