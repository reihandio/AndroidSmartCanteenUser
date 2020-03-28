package reihandio.dev.androidsmartkantin;

import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.widget.FButton;
import reihandio.dev.androidsmartkantin.Common.Common;
import reihandio.dev.androidsmartkantin.Database.Database;
import reihandio.dev.androidsmartkantin.Model.MyResponse;
import reihandio.dev.androidsmartkantin.Model.Notification;
import reihandio.dev.androidsmartkantin.Model.Order;
import reihandio.dev.androidsmartkantin.Model.Request;
import reihandio.dev.androidsmartkantin.Model.Sender;
import reihandio.dev.androidsmartkantin.Model.Token;
import reihandio.dev.androidsmartkantin.Remote.APIService;
import reihandio.dev.androidsmartkantin.ViewHolder.CartAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView txtTotalPrice;
    FButton btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    APIService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        //Inisiasi Service Notifikasi
        mService = Common.getFCMService();

        //Firebase
        database = FirebaseDatabase.getInstance();
        requests=database.getReference("Requests");

        //Inisiasi
        recyclerView = (RecyclerView)findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = (TextView)findViewById(R.id.total);
        btnPlace = (FButton)findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Membuat Request Baru
                    if (cart.size()>0)
                        showAlertDialog();
                    else
                        Toast.makeText(Cart.this, "Keranjang Anda Kosong!", Toast.LENGTH_SHORT).show();



            }
        });

        loadListFood();
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("Selangkah Lagi!");
        alertDialog.setMessage("Masukan Alamat Anda: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment,null);

        final MaterialEditText editAddress = (MaterialEditText)order_address_comment.findViewById(R.id.editAddress);
        final MaterialEditText editComment = (MaterialEditText)order_address_comment.findViewById(R.id.editComment);

        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.cart);

        alertDialog.setPositiveButton("YA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        editAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        editComment.getText().toString(),
                        "0",//status
                        cart
                );

                //Submit ke firebase
                String order_number = String.valueOf(System.currentTimeMillis());
                requests.child(order_number)
                        .setValue(request);

                //Delete Cart
                new Database(getBaseContext()).cleanCart();
                sendNotificationOrder(order_number);


//                Toast.makeText(Cart.this,"Terima Kasih, Order Anda Segera Diproses", Toast.LENGTH_SHORT).show();
//                finish();
            }
        });

        alertDialog.setNegativeButton("TIDAK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true); // mengambil node dengan serverToken "true"
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren())
                {
                    Token serverToken = postSnapShot.getValue(Token.class);

                    //memanggil notifikasi
                    Notification notification = new Notification("Smart-Kantin UMB","Anda Mempunyai Pesanan Baru "+order_number);
                    Sender content = new Sender(serverToken.getToken(), notification);

                    mService.sendNotification(content)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if(response.code() == 200) {
                                        if (response.body().success == 1) {
                                            Toast.makeText(Cart.this, "Terima Kasih, Order Anda Segera Diproses", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(Cart.this, "Order Gagal, Silahkan Ulangi Kembali!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("Terjadi Kesalahan",t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart,this);
        //refresh setelah jd perubahan (setelah delete cart)
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //hitung
        int total = 0;
        for (Order order:cart)
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("in", "ID");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE))
            deleteCart(item.getOrder());
        return true;
    }

    private void deleteCart(int position) {
        //hapus item dari list order dari posisi
        cart.remove(position);
        //hapus data dari sqlite
        new Database(this).cleanCart();
        for (Order item:cart)
            new Database(this).addToCart(item);
        //Refresh
        loadListFood();

    }
}