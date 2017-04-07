package filtre_anti_spam;

import java.util.ArrayList;

public class FiltreAntiSpam {
	
	public double[] bSpam;
	public double[] bHam;
	
	public double PSpam;
	public double PHam;

	public FiltreAntiSpam() {
		// TODO Auto-generated constructor stub
	}
	
	public void apprentissage() {
		//TODO get apprentissage vector
		
		//TODO GET VARIABLE
		int dicoSize = 1000;
		int nbSpam = 500;
		int nbHam = 500;
		int nbMail = nbSpam + nbHam;
		String SpamDirectory = "baseapp/spam";
		String HamDirectory = "baseapp/ham";
		
		int[] apparitionMotsSpam = new int[dicoSize]; 
		int[] apparitionMotsHam = new int[dicoSize]; 
		
		this.bSpam = new double[dicoSize];
		this.bHam = new double[dicoSize];
		
		//apparition des mots dans les spams
		for(int i=0; i<nbSpam; i++){
			this.apprentissage1Mail(SpamDirectory + "i" + ".txt", apparitionMotsSpam);
		}
		
		//apparition des mots dans les hams
		for(int i=0; i<nbHam; i++){
			this.apprentissage1Mail(HamDirectory + "i" + ".txt", apparitionMotsHam);
		}
		
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
	
	public void apprentissage1Mail(String DirectoryName, int[] apparitionMots) {
		//	TODO readFile, remplir le tableau: +1 pour le mot si le spam contient le mot
		
	}
	
	public void verifyMail(String path) {
		//	TODO read file and gate binary vector x
		
		int dicoSize = 100;
		double PMailSpam = 0;
		double PMailHam = 0;
		
		//SPAM
		for(int i=0; i<dicoSize; i++){
			//TODO aurel
		}
		PMailSpam = Math.log(this.PSpam) + PMailSpam;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
