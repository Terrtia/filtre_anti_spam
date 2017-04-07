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

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
