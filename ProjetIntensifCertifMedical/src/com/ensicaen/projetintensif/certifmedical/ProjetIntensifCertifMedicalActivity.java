package com.ensicaen.projetintensif.certifmedical;

import java.io.IOException;
import java.io.InputStream;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.security.cert.X509Certificate;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.ensicaen.projetintensif.certifmedical.qrcode.IntentIntegrator;
import com.ensicaen.projetintensif.certifmedical.qrcode.IntentResult;

public class ProjetIntensifCertifMedicalActivity extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		this.findViewById(R.id.launchScan).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				IntentIntegrator integrator = new IntentIntegrator();
				integrator.initiateScan(ProjetIntensifCertifMedicalActivity.this);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (scanResult != null) {
			// handle scan result
			String toDecipher = scanResult.getContents();
			Log.d("DEBUGTAG","QRCode récupéré en base 64 : " + toDecipher);

			try{
				byte[] toDecipherB64 = Base64.decode(toDecipher.getBytes(), Base64.DEFAULT);
				InputStream insPub = getResources().openRawResource(R.raw.publickey) ;
				X509Certificate cert = X509Certificate.getInstance(insPub);
				PublicKey pub = cert.getPublicKey();
				//Log.d("DEBUGTAG",pub.toString());

				Cipher rsaCipher = Cipher.getInstance("RSA");
				rsaCipher.init(Cipher.DECRYPT_MODE, pub);
				byte[] decrypt = null;
				Log.d("DEBUGTAG","QRCode récup en base normale : "+new String(toDecipherB64));
				decrypt = rsaCipher.doFinal(toDecipherB64);
				Log.d("DEBUGTAG","QRCode décrypté : " + decrypt);
				Log.d("DEBUGTAG","QRCode décrypté avec new String() : " + new String(decrypt));
				Toast.makeText(this, new String(decrypt)	, Toast.LENGTH_LONG).show();
			}catch(Exception e) {
				Log.d("DEBUGTAG",e.toString());
			}
		}else{
			Toast.makeText(this, "Problème lors de la lecture du QRCode"	, Toast.LENGTH_LONG).show();	
		}
	}


}