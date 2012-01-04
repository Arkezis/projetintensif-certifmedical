package com.ensicaen.projetintensif.certifmedical;

import java.io.InputStream;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.security.cert.X509Certificate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ensicaen.projetintensif.certifmedical.qrcode.IntentIntegrator;
import com.ensicaen.projetintensif.certifmedical.qrcode.IntentResult;

public class ProjetIntensifCertifMedicalActivity extends Activity {

	
	static String QRCodeVersion="1.0b";

	TextView tvInfo1 ,tvInfo2;
	LinearLayout llNomPrénom, llDateNaissance, llDateValidité,llAptitude;

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
				String[] elemsQRCode = textFromQR.split(";");

				// Checking if the QRCode is a good one 
				if(	elemsQRCode.length!= 6	|| elemsQRCode[0].equals(ProjetIntensifCertifMedicalActivity.QRCodeVersion)==false	){ 
					Toast.makeText(this, "Le QRCode n'est pas correct. Cela peut être dû à une modification de la clé privée du logiciel, dans ce cas, une nouvelle version de l'application est disponible sur le market.", Toast.LENGTH_LONG).show();
					((TextView) this.findViewById(R.id.tvInfo1)).setVisibility(View.INVISIBLE);		
					((LinearLayout) this.findViewById(R.id.llNomPrénom)).setVisibility(View.INVISIBLE);
					((LinearLayout) this.findViewById(R.id.llDateCertif)).setVisibility(View.INVISIBLE);
					((LinearLayout) this.findViewById(R.id.llDateNaissance)).setVisibility(View.INVISIBLE);
					((LinearLayout) this.findViewById(R.id.llAptitude)).setVisibility(View.INVISIBLE);
					((TextView) this.findViewById(R.id.tvInfo2)).setVisibility(View.INVISIBLE);

				}else{
					tvInfo1 = (TextView) this.findViewById(R.id.tvInfo1);
					tvInfo1.setVisibility(View.VISIBLE);		
					llNomPrénom= (LinearLayout) this.findViewById(R.id.llNomPrénom);
					llNomPrénom.setVisibility(View.VISIBLE);
					((TextView) this.findViewById(R.id.tvPrénom)).setText(elemsQRCode[1]);
					((TextView) this.findViewById(R.id.tvNom)).setText(elemsQRCode[2]);
					llDateNaissance = (LinearLayout) this.findViewById(R.id.llDateNaissance);
					llDateNaissance.setVisibility(View.VISIBLE);
					((TextView) this.findViewById(R.id.tvDateNaissance)).setText(elemsQRCode[3]);
					llDateValidité = (LinearLayout) this.findViewById(R.id.llDateCertif);
					llDateValidité.setVisibility(View.VISIBLE);
					((TextView) this.findViewById(R.id.tvDateValidité)).setText(elemsQRCode[4]);
					llAptitude = (LinearLayout) this.findViewById(R.id.llAptitude);
					llAptitude.setVisibility(View.VISIBLE);
					((TextView) this.findViewById(R.id.tvAptitude)).setText(elemsQRCode[5]);
					tvInfo2 = (TextView) this.findViewById(R.id.tvInfo2);
					tvInfo2.setVisibility(View.VISIBLE);		
				}
			}catch(Exception e) {
				Log.d("DEBUGTAG",e.toString());
			}
		}else{
			Toast.makeText(this, "Problème lors de la lecture du QRCode"	, Toast.LENGTH_LONG).show();	
		}
	}


}