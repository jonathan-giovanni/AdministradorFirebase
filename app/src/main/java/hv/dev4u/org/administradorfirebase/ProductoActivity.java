package hv.dev4u.org.administradorfirebase;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.HashMap;

public class ProductoActivity extends AppCompatActivity {

    //controles en la vista
    TextView lblTitulo;
    EditText txtNombre,txtPrecio;
    Button btnGuardar,btnCancelar;

    //producto recibido de la lista
    Producto producto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto);


        //inicializando controles
        lblTitulo   = findViewById(R.id.lblTitulo);
        txtNombre   = findViewById(R.id.txtNombreProducto);
        txtPrecio   = findViewById(R.id.txtPrecioProducto);
        btnGuardar  = findViewById(R.id.btnGuardarProducto);
        btnCancelar = findViewById(R.id.btnCancelarProducto);



        //Obtengo el producto recibido de la lista
        producto    = (Producto) getIntent().getSerializableExtra("PRODUCTO");

        //si el producto es diferente de null entonces procedo a llenar los editText
        if(producto!=null){
            lblTitulo.setText("Producto: "+producto.getNombre());
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(producto.getPrecio());
        }


        //evento clic de los botones
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductoActivity.this.finish();
            }
        });

        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarProducto();
            }
        });
    }


    private void guardarProducto(){
        //si los campos son validos
        if(camposValido()){
            String nombre = txtNombre.getText().toString();
            String precio = txtPrecio.getText().toString();

            HashMap<String,Object> productoBD = new HashMap<>();
            productoBD.put("nombre",nombre);
            productoBD.put("precio",precio);

            DocumentReference referenciaProducto;
            //si el producto que tengo no es nulo es por que es una edicion
            if(producto!=null){
                referenciaProducto  = MainActivity.dbProductos.document(producto.getId_producto());
            }else{
                referenciaProducto  = MainActivity.dbProductos.document();
            }
            referenciaProducto.set(productoBD)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(ProductoActivity.this, "Producto Guardado", Toast.LENGTH_SHORT).show();
                            ProductoActivity.this.finish();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductoActivity.this, "Ha ocurrido un error al intentar guardar", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            Toast.makeText(this, "Debe llenar los campos", Toast.LENGTH_LONG).show();
        }


    }

    //si txtPrecio y txtNombre no estan vacios entonces son validos
    private boolean camposValido(){
        return !txtPrecio.getText().toString().isEmpty() && !txtNombre.getText().toString().isEmpty();
    }
}
