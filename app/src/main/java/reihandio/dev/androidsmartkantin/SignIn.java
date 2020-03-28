package reihandio.dev.androidsmartkantin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import io.paperdb.Paper;
import reihandio.dev.androidsmartkantin.Common.Common;
import reihandio.dev.androidsmartkantin.Model.User;

public class SignIn extends AppCompatActivity {

    EditText editPhone, editPassword;
    Button btnSignIn;
    com.rey.material.widget.CheckBox ckbRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editPassword = (MaterialEditText)findViewById(R.id.editPassword);
        editPhone = (MaterialEditText)findViewById(R.id.editPhone);
        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        ckbRemember = (com.rey.material.widget.CheckBox) findViewById(R.id.ckbRemember);

        //Inisiasi Paper untuk Remember User
        Paper.init(this);

        //inisiasi Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    //Save User dan Password
                    {
                        Paper.book().write(Common.USER_KEY,editPhone.getText().toString());
                        Paper.book().write(Common.PWD_KEY,editPassword.getText().toString());
                    }



                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            //cek user jika tidak ada di database
                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {

                                //get informasi user
                                mDialog.dismiss();
                                User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                                user.setPhone(editPhone.getText().toString());//set Phone
                                if (user.getPassword().equals(editPassword.getText().toString())) {
                                    Intent homeIntent = new Intent(SignIn.this, Home.class);
                                    Common.currentUser = user;
                                    startActivity(homeIntent);
                                    finish();
                                } else {
                                    Toast.makeText(SignIn.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                mDialog.dismiss();
                                Toast.makeText(SignIn.this, "User doesn't exist!", Toast.LENGTH_SHORT).show();
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    Toast.makeText(SignIn.this, "Mohon Cek Koneksi Anda!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }


        }

        );
    }
}
