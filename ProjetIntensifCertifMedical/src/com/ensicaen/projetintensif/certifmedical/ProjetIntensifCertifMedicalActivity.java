package com.ensicaen.projetintensif.certifmedical;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.security.cert.X509Certificate;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ensicaen.projetintensif.certifmedical.qrcode.IntentIntegrator;
import com.ensicaen.projetintensif.certifmedical.qrcode.IntentResult;

public class ProjetIntensifCertifMedicalActivity extends Activity {


	static String QRCodeVersion="1.0b";
	MessageDigest md = null;
	String[] elemsQRCode;
	TextView tvInfo1 ,tvInfo2;
	LinearLayout llNomPrenom, llDateNaissance, llDateValidite,llAptitude;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.findViewById(R.id.launchScan).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Run the BarcodeScanner intent
				IntentIntegrator integrator = new IntentIntegrator();
				integrator.initiateScan(ProjetIntensifCertifMedicalActivity.this);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			String toDecipherb64 = scanResult.getContents();
			String textFromQR=null;
			try{
				byte[] toDecipherbNormale = Base64.decode(toDecipherb64.getBytes(), Base64.NO_PADDING);

				// Get the publickey and use it to decode
				InputStream insPub = getResources().openRawResource(R.raw.publickey) ;
				PublicKey pub = X509Certificate.getInstance(insPub).getPublicKey();
				Cipher rsaCipher = Cipher.getInstance("RSA/None/NoPadding");
				rsaCipher.init(Cipher.DECRYPT_MODE, pub);
				byte[] decrypt = null;
				decrypt = rsaCipher.doFinal(toDecipherbNormale);

				textFromQR = new String(decrypt);
				elemsQRCode = textFromQR.split(";");

				// Checking if the QRCode is a good one 
				if(	elemsQRCode.length!= 7	|| elemsQRCode[0].equals(ProjetIntensifCertifMedicalActivity.QRCodeVersion)==false	){ 
					Toast.makeText(this, "Le QRCode n'est pas correct. Cela peut être dû à une modification de la cle privee du logiciel, dans ce cas, une nouvelle version de l'application est disponible sur le market.", Toast.LENGTH_LONG).show();
					((TextView) this.findViewById(R.id.tvInfo1)).setVisibility(View.INVISIBLE);		
					((LinearLayout) this.findViewById(R.id.llNomPrenom)).setVisibility(View.INVISIBLE);
					((LinearLayout) this.findViewById(R.id.llDateCertif)).setVisibility(View.INVISIBLE);
					((LinearLayout) this.findViewById(R.id.llDateNaissance)).setVisibility(View.INVISIBLE);
					((LinearLayout) this.findViewById(R.id.llAptitude)).setVisibility(View.INVISIBLE);
					((TextView) this.findViewById(R.id.tvInfo2)).setVisibility(View.INVISIBLE);
				}else{
					// Checking the PINCODE before showing informations
					LayoutInflater factory = LayoutInflater.from(this);
					final View alertDialogView = factory.inflate(R.layout.dialogpin, null);

					AlertDialog.Builder adb = new AlertDialog.Builder(this);
					adb.setView(alertDialogView);
					adb.setTitle("Saisie du PIN du patient");
					adb.setIcon(android.R.drawable.ic_dialog_alert);
					adb.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//Lorsque l'on cliquera sur le bouton "OK", on recupère l'EditText correspondant à notre vue personnalisee (cad à alertDialogView)
							EditText valsaisie = (EditText)alertDialogView.findViewById(R.id.dialogpin_pin);
							String PINtoSha = valsaisie.getText().toString();
							try {
								md = MessageDigest.getInstance("SHA-1");
								md.update(PINtoSha.getBytes("UTF-8"));
							} catch (Exception e) {
								e.printStackTrace();
							}
							String pouet = new String(byteArrayToHexString(md.digest()));
							Log.d("DEBUGTAG","pin : "+PINtoSha+" sha1é : "+pouet+" et l'autre "+elemsQRCode[6]);
							Toast.makeText(ProjetIntensifCertifMedicalActivity.this,"QR : "+elemsQRCode[6]+" saisi : "+pouet,Toast.LENGTH_LONG).show();
							if(pouet.equals(elemsQRCode[6])){
								Toast.makeText(ProjetIntensifCertifMedicalActivity.this,"PIN correct ! ", Toast.LENGTH_LONG).show();
								tvInfo1 = (TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvInfo1);
								tvInfo1.setVisibility(View.VISIBLE);		
								llNomPrenom= (LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llNomPrenom);
								llNomPrenom.setVisibility(View.VISIBLE);
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvPrenom)).setText(elemsQRCode[1]);
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvNom)).setText(elemsQRCode[2]);
								llDateNaissance = (LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llDateNaissance);
								llDateNaissance.setVisibility(View.VISIBLE);
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvDateNaissance)).setText(elemsQRCode[3]);
								llDateValidite = (LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llDateCertif);
								llDateValidite.setVisibility(View.VISIBLE);
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvDateValidite)).setText(elemsQRCode[4]);
								llAptitude = (LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llAptitude);
								llAptitude.setVisibility(View.VISIBLE);
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvAptitude)).setText(elemsQRCode[5]);
								tvInfo2 = (TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvInfo2);
								tvInfo2.setVisibility(View.VISIBLE);		
							}else{
								Toast.makeText(ProjetIntensifCertifMedicalActivity.this, "mauvais pin !", Toast.LENGTH_LONG).show();
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvInfo1)).setVisibility(View.INVISIBLE);		
								((LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llNomPrenom)).setVisibility(View.INVISIBLE);
								((LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llDateCertif)).setVisibility(View.INVISIBLE);
								((LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llDateNaissance)).setVisibility(View.INVISIBLE);
								((LinearLayout) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.llAptitude)).setVisibility(View.INVISIBLE);
								((TextView) ProjetIntensifCertifMedicalActivity.this.findViewById(R.id.tvInfo2)).setVisibility(View.INVISIBLE);
							}
						} });

					adb.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							//Lorsque l'on cliquera sur annuler on quittera l'application
							//finish();
						} });
					adb.show();


				}
			}catch(Exception e) {
				Log.d("DEBUGTAG",e.toString());
			}
		}else{
			Toast.makeText(this, "Problème lors de la lecture du QRCode"	, Toast.LENGTH_LONG).show();	
		}
	}

	public static String byteArrayToHexString(byte[] array) {
		StringBuffer hexString = new StringBuffer();
		for (byte b : array) {
			int intVal = b & 0xff;
			if (intVal < 0x10)
				hexString.append("0");
			hexString.append(Integer.toHexString(intVal));
		}
		return hexString.toString(); 
	}


}