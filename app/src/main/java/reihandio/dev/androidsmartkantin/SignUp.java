package reihandio.dev.androidsmartkantin;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import reihandio.dev.androidsmartkantin.Common.Common;
import reihandio.dev.androidsmartkantin.Model.User;

public class SignUp extends AppCompatActivity {

    MaterialEditText editPhone,editName,editPassword;
    Button btnSignUp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        editName = (MaterialEditText)findViewById(R.id.editName);
        editPassword = (MaterialEditText)findViewById(R.id.editPassword);
        editPhone = (MaterialEditText)findViewById(R.id.editPhone);

        btnSignUp = (Button)findViewById(R.id.btnSignUp);

        //inisiasi Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if (Common.isConnectedToInternet(getBaseContext())) {
                  final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                  mDialog.setMessage("Please Wait...");
                  mDialog.show();

                  table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          //Check jika user sudah terdaftar
                          if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                              mDialog.dismiss();
                              Toast.makeText(SignUp.this, "Phone Number Already Registered!", Toast.LENGTH_SHORT).show();
                          } else {
                              mDialog.dismiss();
                              User user = new User(editName.getText().toString(), editPassword.getText().toString());
                              table_user.child(editPhone.getText().toString()).setValue(user);
                              Toast.makeText(SignUp.this, "Sign Up Successful!", Toast.LENGTH_SHORT).show();
                              finish();
                          }
                      }

                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {

                      }
                  });
              }
              else
              {
                  Toast.makeText(SignUp.this, "Mohon Cek Koneksi Anda!", Toast.LENGTH_SHORT).show();
                  return;
              }

            }
        });
    }
}
