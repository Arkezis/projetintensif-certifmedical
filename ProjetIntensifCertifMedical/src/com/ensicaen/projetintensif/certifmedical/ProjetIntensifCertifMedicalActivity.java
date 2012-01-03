package com.ensicaen.projetintensif.certifmedical;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
    		  Toast.makeText(this, scanResult.getContents()	, Toast.LENGTH_LONG).show();
    	  }
    	  // else continue with any other code you need in the method
    	}
}