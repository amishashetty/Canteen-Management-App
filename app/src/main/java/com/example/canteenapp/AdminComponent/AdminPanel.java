package com.example.canteenapp.AdminComponent;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.canteenapp.AdminAlgorithm;
import com.example.canteenapp.FoodList.Food;
import com.example.canteenapp.History.HistoryModel;
import com.example.canteenapp.History.itemHistory;
import com.example.canteenapp.MainActivity;
import com.example.canteenapp.OrderList.OrderIteam;
import com.example.canteenapp.QrCodeImplementation.QrCamera;
import com.example.canteenapp.R;
import com.example.canteenapp.Registration.Canteen;
import com.example.canteenapp.Registration.CanteenRegistration;
import com.example.canteenapp.Registration.Registration;
import com.example.canteenapp.Util.CanteenUtil;
import com.example.canteenapp.constant.CanteenConstant;
import com.example.canteenapp.constant.FireBaseConstant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class AdminPanel extends AppCompatActivity {
  CardView viewOderCard, addUserCard, addFoodCard;
  DatabaseReference databaseReference;
  FirebaseDatabase firebaseDatabase;
  String keyEmail,keyPass;
  TextView adminPanelTotalAmount;
  int netIncome = 0;
  private static final DecimalFormat df = new DecimalFormat("0.00");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.admin_panel);
    viewOderCard = findViewById(R.id.viewOrderLogoCard);
    addUserCard = findViewById(R.id.addUserLogoCard);
    addFoodCard = findViewById(R.id.addFoodLogoCard);
    firebaseDatabase = FirebaseDatabase.getInstance();
    databaseReference = firebaseDatabase.getReference(FireBaseConstant.HISTORY);
    adminPanelTotalAmount = findViewById(R.id.adminPanelTotalAmount);
    keyEmail=getIntent().getStringExtra("keyEmail");
    keyPass=getIntent().getStringExtra("keyPass");
    System.out.println(keyEmail);
    System.out.println(keyPass);

    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED) {
      ActivityCompat.requestPermissions(AdminPanel.this, new String[]{Manifest.permission.CAMERA}, 1);
    }
    databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if (snapshot.getValue() != null) {
          netIncome = 0;
          for (DataSnapshot s : snapshot.getChildren()) {
            if (CanteenUtil.getYearAndDayFromMilliSecond(System.currentTimeMillis()).equals(CanteenUtil.getYearAndDayFromMilliSecond(Long.parseLong(s.child("time").getValue().toString())))) {
              HistoryModel historyModel = s.getValue(HistoryModel.class);
              if (null != historyModel && historyModel.getComment().contains(CanteenConstant.ADMIN)) {
                netIncome = netIncome + Integer.parseInt(historyModel.getTotal());
              }
            } else {
              databaseReference.child(s.getKey()).removeValue();
            }
          }
          adminPanelTotalAmount.setText("Rs." + NumberFormat.getInstance().format(Float.parseFloat(df.format(netIncome))));
          databaseReference.removeEventListener(this);
        }
        databaseReference.removeEventListener(this);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        databaseReference.removeEventListener(this);
      }
    });

    viewOderCard.setOnClickListener(v -> {
      Intent intent = new Intent(getApplicationContext(), OrderIteam.class);

      intent.putExtra("keyEmail",keyEmail);
      intent.putExtra("keyPass",keyPass);
      startActivity(intent);
    });
    addUserCard.setOnClickListener(v -> {
      Intent intent = new Intent(getApplicationContext(), CanteenRegistration.class);
      startActivity(intent);
    });
    addFoodCard.setOnClickListener(v -> {
      Intent intent = new Intent(getApplicationContext(), Food.class);
      intent.putExtra("keyEmail", keyEmail);
      intent.putExtra("keyPass",keyPass);
      startActivity(intent);
    });
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finishAffinity();
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);

  }
}
