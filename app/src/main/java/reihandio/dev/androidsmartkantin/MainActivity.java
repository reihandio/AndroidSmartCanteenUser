package reihandio.dev.androidsmartkantin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.paperdb.Paper;
import reihandio.dev.androidsmartkantin.Common.Common;
import reihandio.dev.androidsmartkantin.Model.User;

public class MainActivity extends AppCompatActivity {


    Button btnSignIn, btnSignUp;
    TextView txtSlogan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSignIn = (Button)findViewById(R.id.btnSignIn);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);

        txtSlogan = (TextView)findViewById(R.id.txtSlogan);
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/SUNDAY  Personal use.ttf");
        txtSlogan.setTypeface(face);

//        btnSignIn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });

        //Inisiasi Paper untuk Remember User
        Paper.init(this);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUp = new Intent (MainActivity.this,SignUp.class);
                startActivity(signUp);
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signIn = new Intent (MainActivity.this,SignIn.class);
                startActivity(signIn);
            }
        });

        //Cek Remember
        String user = Paper.book().read(Common.USER_KEY);
        String pwd = Paper.book().read(Common.PWD_KEY);
        if(user != null && pwd != null)
        {
            if(!user.isEmpty() & !pwd.isEmpty())
                login(user,pwd);
        }

    }



    private void login(final String phone, final String pwd) {

        if (Common.isConnectedToInternet(getBaseContext())) {

            //inisiasi Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference table_user = database.getReference("User");

            //Save User dan Password
            {
                Paper.book().write(Common.USER_KEY,phone);
                Paper.book().write(Common.PWD_KEY,pwd);
            }



            final ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Mohon Tunggu...");
            mDialog.show();

            table_user.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //cek user jika tidak ada di database
                    if (dataSnapshot.child(phone).exists()) {

                        //get informasi user
                        mDialog.dismiss();
                        User user = dataSnapshot.child(phone).getValue(User.class);
                        user.setPhone(phone);//set Phone
                        if (user.getPassword().equals(pwd)){
                            Intent homeIntent = new Intent(MainActivity.this, Home.class);
                            Common.currentUser = user;
                            startActivity(homeIntent);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Password Salah", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        mDialog.dismiss();
                        Toast.makeText(MainActivity.this, "User Tidak Ada!", Toast.LENGTH_SHORT).show();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        else
        {
            Toast.makeText(MainActivity.this, "Mohon Cek Koneksi Anda!", Toast.LENGTH_SHORT).show();
            return;
        }
    }
}
