package hv.dev4u.org.administradorfirebase;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.HashMap;

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

            //es por que tiene una imagen
            if(producto.getImagenProducto()!=null){
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
                guardarProducto();
            }
        });

        btnCambiarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cambiarImg();
            }
        });

        btnEliminarImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarImagen();
            }
        });

        imgRotar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bitmapProducto!=null){
                    cambioImagen=true;
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    bitmapProducto = Bitmap.createBitmap(bitmapProducto, 0, 0, bitmapProducto.getWidth(), bitmapProducto.getHeight(), matrix, true);
                    imgProducto.setImageBitmap(bitmapProducto);
                }
            }
        });

    }


    private void guardarImagen(final DocumentReference referencia, final HashMap<String,Object> datos){

        if(cambioImagen && bitmapProducto!=null){

            String id_img = referencia.getId();
            //crea una referencia la cual se llamara como el id del documento
            final StorageReference refImgFirebase = MainActivity.imgFirebase.child(id_img+".jpg");
            //convierto la imagen a un array de bytes y la subo a firebase
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapProducto.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageInByte = stream.toByteArray();

            //supervisa el estado de la carga
            UploadTask uploadTask = refImgFirebase.putBytes(imageInByte);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            datos.put("ruta_imagen",refImgFirebase.getPath());
                            subirDatos(referencia,datos);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProductoActivity.this, "Error al subir imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
        }else{
            subirDatos(referencia,datos);
        }
    }

    private void subirDatos(final DocumentReference referencia, final HashMap<String,Object> datos){
        referencia.set(datos)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProductoActivity.this, "Producto Guardado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProductoActivity.this, "Ha ocurrido un error al intentar guardar", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void guardarProducto(){

        if(camposValido()) {

            DocumentReference referenciaProducto;
            //si el producto que tengo no es nulo es por que es una edicion
            if(producto!=null){
                referenciaProducto  = MainActivity.dbProductos.document(producto.getId_producto());
            }else{
                referenciaProducto  = MainActivity.dbProductos.document();
            }

            String nombre = txtNombre.getText().toString();
            String precio = txtPrecio.getText().toString();

            HashMap<String,Object> productoBD = new HashMap<>();
            productoBD.put("nombre",nombre);
            productoBD.put("precio",precio);


            guardarImagen(referenciaProducto,productoBD);

        }else{
            Toast.makeText(this, "Debe llenar los campos", Toast.LENGTH_LONG).show();
        }

    }

    private void cambiarImg() {
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
                bmp = getBitmapFromUri(selectedImage);
            } catch (IOException e) {
                Toast.makeText(this,"Error al cargar imagen",Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            if(bmp!=null){
                //se reduce la imagen
                bmp = reducirImagen(bmp);
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



    private void eliminarImagen(){

        if(producto.getRuta_imagen()!=null){
            //proceder a eliminar de la BD
            
        }else{
            limpiarImagen();
        }
    }

    private void limpiarImagen(){
        bitmapProducto=null;
        producto.setImagenProducto(null);
        imgProducto.setImageResource(R.drawable.ic_producto);
        btnEliminarImg.setEnabled(false);
    }
    

    //si txtPrecio y txtNombre no estan vacios entonces son validos
    private boolean camposValido(){
        return !txtPrecio.getText().toString().isEmpty() && !txtNombre.getText().toString().isEmpty();
    }


    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private Bitmap reducirImagen(Bitmap original){
        //reduciendo imagen a formato JPG y con calidad del 70%
        Bitmap reducedBitmap = original;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] imageInByte = stream.toByteArray();
        float lengthbmp = imageInByte.length;
        lengthbmp /= 1024;//to Kb
        //reduciendo aun mas a 1/4 del tamanio
        reducedBitmap = Bitmap.createScaledBitmap (reducedBitmap,(int) (reducedBitmap.getWidth() * .4), (int) (reducedBitmap.getHeight() * .4),true);

        //si la imagen pesa mas de 200kb se reduce aun mas
        if(lengthbmp>200.00){
            stream = new ByteArrayOutputStream();
            reducedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream);
            imageInByte = stream.toByteArray();
            lengthbmp = imageInByte.length;
            lengthbmp /= 1024;//to Kb
        }
        //si la imagen apesar de las reducciones pesa mas de 1MB entonces no es una imagen valida
        if(lengthbmp>1024){
            Toast.makeText(this, "Imagen demasiado pesada, intenta con otra", Toast.LENGTH_LONG).show();
            return null;
        }else{
            return reducedBitmap;
        }
    }
}
