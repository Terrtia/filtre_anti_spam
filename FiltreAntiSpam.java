package filtre_anti_spam;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FiltreAntiSpam {
	
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
			InputStream ips=new FileInputStream(fichier); 
			InputStreamReader ipsr=new InputStreamReader(ips);
			BufferedReader br=new BufferedReader(ipsr);
			
			while ((ligne=br.readLine())!=null){
				System.out.println(ligne);
				if(ligne.length() >= 3){
					dico.add(ligne);
				}
			}
			br.close(); 
		}		
		catch (Exception e){
			System.out.println(e.toString());
		}
		
	}
	
	public boolean[] lireMessage(String fichier){
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
			System.out.println(e.toString());
		}
		
		return message;
	}
	
	public void apprentissage() {
		
		//TODO GET VARIABLE
		int dicoSize = 1000;
		int nbSpam = 500;
		int nbHam = 500;
		int nbMail = nbSpam + nbHam;
		String SpamDirectory = "baseapp/spam";
		String HamDirectory = "baseapp/ham";
		
		int[] apparitionMotsSpam = apprentissageMail(SpamDirectory); 
		int[] apparitionMotsHam = apprentissageMail(HamDirectory); 
		
		this.bSpam = new double[dicoSize];
		this.bHam = new double[dicoSize];
		
		
		//SPAM, estimation des probabilites par les frequences
		for(int i=0; i<nbSpam; i++){
			this.bSpam[i] = (double) (apparitionMotsSpam[i] / nbSpam);
		}
		
		//HAM, estimation des probabilites par les frequences
		for(int i=0; i<nbHam; i++){
			this.bHam[i] = (double) (apparitionMotsHam[i] / nbHam);
		}
		
		//SPAM, estimation Probabilite a posteriori P(Y = SPAM)
		this.PSpam = (double) (nbSpam / nbMail) ;
		
		//HAM, estimation Probabilite a posteriori P(Y = HAM)
		this.PHam = 1 - this.PSpam;
		
	}
	
	public int[] apprentissageMail(String directoryName) {
		int mots[] = new int[dico.size()];
		for(int i = 0; i < mots.length;i++){
			mots[i] = 0;
		}
		boolean vecteur[];
		String [] files;
		File repertoire = new File(directoryName);
		files=repertoire.list();
		for(String fileName : files){
			vecteur = lireMessage(fileName);
			for(int i = 0; i < vecteur.length;i++){
				if(vecteur[i]){
					mots[i]++;
				}
			}
		}
		return mots;
	}
	
	public void verifyMail(String path) throws Exception {
		//	TODO read file and gate binary vector x
		int[] x = new int[1000]; //provisoire, represente le vecteur booleen x du mail
		// TODO verif du vecteur ?
		
		int dicoSize = 1000;
		double PMailSpam = 0;
		double PMailHam = 0;
		
		int j = -1;
		
		//SPAM
		for(int i=0; i<dicoSize; i++){
			j = x[i];
			if(j == 1){
				PMailSpam = Math.log(this.bSpam[i]) + PMailSpam;
			} else if(j == 0){
				PMailSpam = Math.log(1 - this.bSpam[i]) + PMailSpam;
			} else {
				throw new Exception("Critical Eror");
			}
		}
		PMailSpam = Math.log(this.PSpam) + PMailSpam;
		
		//HAM
		for(int i=0; i<dicoSize; i++){
			j = x[i];
			if(j == 1){
				PMailHam = Math.log(this.bHam[i]) + PMailHam;
			} else if(j == 0){
				PMailHam = Math.log(1 - this.bHam[i]) + PMailHam;
			} else {
				throw new Exception("Critical Eror");
			}
		}
		PMailHam = Math.log(this.PHam) + PMailHam;
		
		// Estimation SPAM ou HAM
		double res = Math.max(PMailSpam, PMailHam);
		if(res == PMailSpam){
			// mail considere comme un SPAM
		} else if(res == PMailHam){
			// mail considere comme un HAM
		} else {
			throw new Exception("Critical Eror");
		}
		
		//TODO affichage
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
