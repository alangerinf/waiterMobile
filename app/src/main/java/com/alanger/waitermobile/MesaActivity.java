package com.alanger.waitermobile;


import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alanger.waitermobile.app.AppController;
import com.alanger.waitermobile.model.Mesa;
import com.alanger.waitermobile.model.Plato;
import com.alanger.waitermobile.model.SharedPreferencesManager;
import com.alanger.waitermobile.model.User;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MesaActivity extends AppCompatActivity {

    static String TAG  = MesaActivity.class.getSimpleName();
    static Mesa mesa;
    static TextView tViewNOrden;
    static TextView tViewCuenta;
    static TextView tView_Importe;
    static ConstraintLayout clContent;
    static AppCompatButton btnCobrar;
    static FloatingActionButton fAButtonAddPlato;
    static int REQUEST_QR_NPALLET=2134;

    static Context ctx;

    final public static String PARAM_MESA = "mesa";


    static Bundle b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mesa);


        b = getIntent().getExtras();
        assert b != null;
        mesa = (Mesa) b.getSerializable(PARAM_MESA);
        assert mesa != null;
        Log.d(TAG, mesa.toString());

        define();
        defineAtribs();

        events();
    }

    private void events() {

        fAButtonAddPlato.setOnClickListener(v->{
            User user = SharedPreferencesManager.getUser(ctx);
            consultarMenu(user.getToken());

        });
        btnCobrar.setOnClickListener(v->{
            Mesa item = mesa;
            Intent i = new Intent(this, CobrarActivity.class);

            ActivityOptions options = (ActivityOptions) ActivityOptions.makeSceneTransitionAnimation
                    (this,
                            Pair.create(btnCobrar, btnCobrar.getTransitionName()),
                            Pair.create(tViewCuenta, tViewCuenta.getTransitionName()),
                            Pair.create(tView_Importe, tView_Importe.getTransitionName()),
                            Pair.create(clContent, clContent.getTransitionName())
                    );
            Bundle bundleExtra = new Bundle();
            bundleExtra.putSerializable(MesaActivity.PARAM_MESA,  item);
            i.putExtras(bundleExtra);
            startActivity(i, options.toBundle());
            v.setClickable(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        btnCobrar.setClickable(true);
    }

    @SuppressLint("SetTextI18n")
    private void defineAtribs() {
        tViewNOrden.setText(""+ mesa.getPosicion());
    }

    private void define() {
        ctx = this;
        fAButtonAddPlato  = findViewById(R.id.mesa_fAButtonAddPlato);
        btnCobrar = findViewById(R.id.mesa_btnCobrar);
        tViewNOrden = findViewById(R.id.mesa_tViewNOrden);
        tViewCuenta = findViewById(R.id.mesa_tViewCuenta);
        tView_Importe = findViewById(R.id.mesa_tView_Importe);
        clContent = findViewById(R.id.mesa_clContentTotal);
        platoList = new ArrayList<>();


    }

    @Override
    public void onBackPressed() {
        /*
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("pallet", (Serializable) mesa);
        i.putExtras(bundle);
        setResult(Activity.RESULT_OK,i);
        super.onBackPressed();
        */
        super.onBackPressed();
        //finish();
    }


    private void showPopupSelectCantidadPlato(Plato plato){
        Dialog dialogClose;
        dialogClose = new Dialog(this);
        dialogClose.setContentView(R.layout.dialog_cantidad_plato);
        Button btnOk = (Button) dialogClose.findViewById(R.id.btnOk);
        ImageView iViewDialogClose = (ImageView) dialogClose.findViewById(R.id.iViewDialogClose);
        TextView tViewRecomendacion = dialogClose.findViewById(R.id.tViewRecomendacion);
        TextView tViewNamePlato = dialogClose.findViewById(R.id.tViewNamePlato);


        tViewNamePlato.setText(plato.getName());
        tViewRecomendacion.setText("Solo se pueden agregar "+plato.getCantidad()+" platos");


        EditText eTextCantidad= dialogClose.findViewById(R.id.eTextCantidad);




        eTextCantidad.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if(eTextCantidad.getText().toString().equals("")||(Integer.valueOf(eTextCantidad.getText().toString())==0&&eTextCantidad.getText().toString().length()>1)){
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eTextCantidad.setText("0");
                            eTextCantidad.setSelection(eTextCantidad.getText().toString().length());
                        }
                    });

                }else {

                    if(Integer.valueOf(eTextCantidad.getText().toString())>plato.getCantidad()){
                        Handler handler = new Handler();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                eTextCantidad.setError("Máximo superado");
                            }
                        });

                    }

                }

                if((!eTextCantidad.getText().toString().equals(""))&&String.valueOf(Integer.valueOf(eTextCantidad.getText().toString())).length()!= eTextCantidad.getText().toString().length() ){
                    Handler handler = new Handler();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eTextCantidad.setText(String.valueOf(Integer.valueOf(eTextCantidad.getText().toString())));
                            eTextCantidad.setSelection(eTextCantidad.getText().toString().length());
                        }
                    });

                }


            }
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {





            }

            public void afterTextChanged(Editable s) {

            }
        });

        iViewDialogClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogClose.dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(eTextCantidad.getText().toString().equals("0")){
                    Toast.makeText(ctx,"No se insertaron Platos",Toast.LENGTH_LONG).show();

                }else {
                    //insertar
                    Toast.makeText(ctx,"Se agregaron "+eTextCantidad.getText().toString()+" Platos",Toast.LENGTH_LONG).show();

                }
                dialogClose.dismiss();

            }
        });

        dialogClose.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialogClose.show();
    }



    private void showDialogMenu(){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(ctx);
        builderSingle.setIcon(R.mipmap.ic_launcher_round);
        builderSingle.setTitle("Lista de Platos Restantes");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(ctx, android.R.layout.simple_list_item_1);

        for(int i=0;i<platoList.size();i++){
            arrayAdapter.add(""+platoList.get(i).getCantidad()+" "+platoList.get(i).getName());
        }

        builderSingle.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String strName = arrayAdapter.getItem(which);


                showPopupSelectCantidadPlato(platoList.get(which));
            }
        });
        builderSingle.show();

    }


    static List<Plato> platoList;

    private void consultarMenu(String token){
        ProgressDialog progressDialog = new ProgressDialog(ctx);
        progressDialog.setTitle("Buscando Mesas");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Log.d(TAG,"entro en consulta");

        String url = ConectionConfig.POST_PLATOS;
        Log.d(TAG,url);
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int codigoRespuesta = response.getInt("codigoRespuesta");
                            if(codigoRespuesta==ConectionConfig.HTTP_OK) {

                                JSONObject datos = new JSONObject(String.valueOf(response.getJSONObject("datos")));
                                JSONArray platos = datos.getJSONArray("platos");
                                platoList.clear();

                                for(int i=0;i<platos.length();i++){
                                    //convertir cada Batch  por GSON
                                    Gson gson = new Gson();
                                    Log.d(TAG,"Plato : "+platos.getJSONObject(i).toString());

                                    JSONObject paltoData = platos.getJSONObject(i);

                                    int idPlato = paltoData.getInt("id_plato");
                                    String nombre = paltoData.getString("nombre");
                                    int precio = paltoData.getInt("precio");
                                    int cantidad = paltoData.getInt("cantidad");

                                    Plato platoTemp = new Plato();
                                    platoTemp.setId(idPlato);
                                    platoTemp.setName(nombre);
                                    platoTemp.setCantidad(cantidad);
                                    platoTemp.setPrecio(precio);

                                    platoList.add(platoTemp);
                                }
                                progressDialog.dismiss();

                                showDialogMenu();

                            }else {
                                if(codigoRespuesta==ConectionConfig.HTTP_ERROR){
                                    Toast.makeText(ctx,"Área sin Sensores",Toast.LENGTH_LONG).show();
                                    // onBackPressed();
                                    progressDialog.dismiss();
                                }
                            }

                        } catch (JSONException e) {
                            Toast.makeText(ctx,"json"+e.toString(),Toast.LENGTH_LONG).show();
                            Log.d(TAG,e.toString());
                            e.printStackTrace();
                            progressDialog.dismiss();
                            // onBackPressed();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx,error.toString(),Toast.LENGTH_LONG).show();
                Log.d(TAG,error.toString());
                error.printStackTrace();
                progressDialog.dismiss();
                //     onBackPressed();
            }

        }){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers = new HashMap<String, String>();
                headers.put("Authorization",token);
                return headers;
            }

        };
        jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AppController.getInstance().addToRequestQueue(jsonObjReq);
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_QR_NPALLET) {

            try {
                Bundle recibidos = (data.getExtras());
                if (recibidos != null) {
                    String qr = recibidos.getString("qr");
                }
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    }
}
