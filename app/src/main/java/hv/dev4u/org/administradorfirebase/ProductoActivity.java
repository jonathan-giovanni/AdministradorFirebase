package hv.dev4u.org.administradorfirebase;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.HashMap;

import hv.dev4u.org.administradorfirebase.utilidades.UtilidadesImagenes;

public class ProductoActivity extends AppCompatActivity {

    //controles en la vista
    TextView lblTitulo;
    EditText txtNombre,txtPrecio;
    Button btnGuardar,btnCancelar,btnCambiarImg,btnEliminarImg;
    ImageView imgProducto,imgRotarIzq, imgRotar;

    //producto recibido de la lista
    Producto producto;
    //identificador del permiso de lectura de imagen
    final int READ_EXTERNAL_STORAGE_PERMISSION_CODE = 23;
    final int IMAGE_CODE_REQUEST=34;
    Bitmap bitmapProducto;
    boolean cambioImagen =false;

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
        //para la imagen
        btnCambiarImg   = findViewById(R.id.btnCambiarImg);
        btnEliminarImg  = findViewById(R.id.btnEliminarImg);
        imgProducto     = findViewById(R.id.imgProducto);
        imgRotar        = findViewById(R.id.imgRotar);
        //por defecto no se puede eliminar la imagen por que no hay
        btnEliminarImg.setEnabled(false);

        //Obtengo el producto recibido de la lista
        producto    = (Producto) getIntent().getSerializableExtra("PRODUCTO");

        //si el producto es diferente de null entonces procedo a llenar los editText
        if(producto!=null){
            lblTitulo.setText("Producto: "+producto.getNombre());
            txtNombre.setText(producto.getNombre());
            txtPrecio.setText(producto.getPrecio());
            //obtengo imagen desde MainActivity
            if(producto.getRuta_imagen()!=null && MainActivity.imgSeleccionada!=null ){
                bitmapProducto  = MainActivity.imgSeleccionada;
                imgProducto.setImageBitmap(bitmapProducto);
                btnEliminarImg.setEnabled(true);
            }
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
                prepararSubidaProducto();
            }
        });

        btnCambiarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                seleccionarImg();
            }
        });

        btnEliminarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                limpiarImagen();
            }
        });

        imgRotar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmapProducto!=null){
                    cambioImagen=true;
                    bitmapProducto = UtilidadesImagenes.rotarImagen(bitmapProducto);
                    imgProducto.setImageBitmap(bitmapProducto);
                }
            }
        });
    }




    //si txtPrecio y txtNombre no estan vacios entonces son validos
    private boolean camposValido(){
        return !txtPrecio.getText().toString().isEmpty() && !txtNombre.getText().toString().isEmpty();
    }



    private void prepararSubidaProducto(){
        if(camposValido()) {
            DocumentReference referenciaProducto;

            String nombre = txtNombre.getText().toString();
            String precio = txtPrecio.getText().toString();

            HashMap<String,Object> productoBD = new HashMap<>();
            productoBD.put("nombre",nombre);
            productoBD.put("precio",precio);

            //si el producto que tengo no es nulo es por que es una edicion
            if(producto!=null){
                referenciaProducto  = MainActivity.dbProductos.document(producto.getId_producto());
            }else{
                referenciaProducto  = MainActivity.dbProductos.document();
            }

            //si la imagen cambio entonces se procede a
            if(cambioImagen){
                actualizarImagen(referenciaProducto,productoBD);
            }else{
                guardarDatos(referenciaProducto,productoBD,true);
            }

        }else{
            Toast.makeText(this, "Debe llenar los campos", Toast.LENGTH_LONG).show();
        }
    }


    private void guardarDatos(final DocumentReference referencia, final HashMap<String,Object> datos, final boolean cerrar){
        referencia.set(datos,SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(cerrar){
                            Toast.makeText(ProductoActivity.this, "Producto guardado", Toast.LENGTH_SHORT).show();
                            ProductoActivity.this.finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductoActivity.this, "Ha ocurrido un error al intentar guardar", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    /** Las siguientes son funciones para la administracion de la imagen*/

    private void actualizarImagen(final DocumentReference referencia, final HashMap<String,Object> datos){


        //id de la imagen
        String id_img = referencia.getId()+".jpg";

        //referencia a la imagen en firebase
        final StorageReference refImgFirebase = MainActivity.imgProductosFirebase.child(id_img);

        if(bitmapProducto!=null ){

            datos.put("ruta_imagen",FieldValue.delete());
            guardarDatos(referencia,datos,false);


            //establecememos la ruta
            datos.put("ruta_imagen",id_img);

            //imagen en bytes
            byte[] imagenEnBytes = UtilidadesImagenes.getImagenEnBytes(bitmapProducto);

            //supervisa el estado de la carga
            refImgFirebase
                    .putBytes(imagenEnBytes)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            guardarDatos(referencia,datos,true);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductoActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                        }
                    });


            //en caso de liminacion, si el producto tiene datos
        }else if(producto!=null){
            //si tiene una ruta de imagen entonces se borra
            if(producto.getRuta_imagen()!=null){
                datos.put("ruta_imagen",FieldValue.delete());

                //borrando de firebase
                refImgFirebase
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                guardarDatos(referencia,datos,true);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        }
    }

    private void seleccionarImg() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            //ask for permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE_PERMISSION_CODE);
            }
        }else{
            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_CODE_REQUEST);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_CODE_REQUEST && resultCode==RESULT_OK){

            Uri selectedImage = data.getData();
            Bitmap bmp = null;
            try {
                bmp = UtilidadesImagenes.getImagenDesdeRuta(getApplicationContext(),selectedImage);
            } catch (IOException e) {
                Toast.makeText(this,"Error al cargar imagen",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            if(bmp!=null){
                //se reduce la imagen
                bmp = UtilidadesImagenes.reducirImagen(bmp);
                //si la reduccion fue exitosa retorna un bitmap reducido
                if(bmp!=null){
                    cambioImagen =true;
                    bitmapProducto = bmp;
                    imgProducto.setImageBitmap(bitmapProducto);
                    btnEliminarImg.setEnabled(true);
                }
            }
        }
    }

    private void limpiarImagen(){
        cambioImagen=true;
        bitmapProducto=null;
        producto.setImagenProducto(null);
        imgProducto.setImageResource(R.drawable.ic_producto);
        btnEliminarImg.setEnabled(false);
    }

}
