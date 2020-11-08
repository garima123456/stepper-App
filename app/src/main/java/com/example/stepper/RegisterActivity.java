package com.example.stepper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class RegisterActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueAndNextBtn;
    private String checker="",phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private  PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        loadingBar= new ProgressDialog(RegisterActivity.this);

        phoneText=findViewById(R.id.phoneText);
        codeText=findViewById(R.id.codeText);
        continueAndNextBtn=findViewById(R.id.continueNextButton);
        relativeLayout=findViewById(R.id.phoneAuth);
        ccp=(CountryCodePicker) findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);
        continueAndNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (continueAndNextBtn.getText().equals("Submit")|| checker.equals(("code Sent")))
                {
                    String verificationCode=codeText.getText().toString();
                    if (verificationCode.equals("")){
                        Toast.makeText(RegisterActivity.this, "Please write  verification code first", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("please wait, while we are verifying your code");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
                else{
                    phoneNumber = ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals(""))
                    {
                        loadingBar.setTitle("Phone Number Verification");
                        loadingBar.setMessage("please wait, while we are verifying your phone number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,
                                60,
                                TimeUnit.SECONDS,
                                RegisterActivity.this,
                                mCallbacks);      // OnVerificationStateChangedCallbacks
                    }
                    else{
                        Toast.makeText(RegisterActivity.this, "Please enter valid phone number", Toast.LENGTH_SHORT).show();
                    }
                }}
        });
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            private static final String TAG = "RegisterActivity";

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(RegisterActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                relativeLayout.setVisibility(View.VISIBLE);


                continueAndNextBtn.setText("Continue");
                codeText.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                mVerificationId=s;
                mResendToken=forceResendingToken;

                relativeLayout.setVisibility(View.GONE);
                checker="Code sent";
                continueAndNextBtn.setText("Submit");
                codeText.setVisibility(View.VISIBLE);
                loadingBar.dismiss();
                Toast.makeText(RegisterActivity.this, "Code has been sent, please check and enter here", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this, "Congratulations", Toast.LENGTH_SHORT).show();
                            sendUserTOMainActivity();
                            // ...
                        } else {
                            loadingBar.dismiss();
                            String e=task.getException().toString();
                            Toast.makeText(RegisterActivity.this, "Error : "+e, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void sendUserTOMainActivity(){
        Intent intent= new Intent(RegisterActivity.this, ContactsActivity.class);
        startActivity(intent);
        finish();
    }
}