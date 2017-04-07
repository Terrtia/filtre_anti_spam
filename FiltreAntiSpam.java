package filtre_anti_spam;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FiltreAntiSpam {
	
	public double[] bSpam;
	public double[] bHam;
	private ArrayList<String> dico;

	public FiltreAntiSpam() {
		dico = new ArrayList<>();
	}
	
	public void chargerDico(String fichier){
		String ligne;
		
		//lecture du fichier texte	
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
		
		//lecture du fichier texte	
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
		//TODO get apprentissage vector
		
		//TODO GET VARIABLE
		int dicoSize = 1000;
		int nbSpam = 500;
		int nbHam = 500;
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
		
		
		
	}
	
	public void apprentissage1Mail(String DirectoryName, int[] apparitionMots) {
		//	TODO readFile, remplir le tableau: +1 pour le mot si le spam contient le mot
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
