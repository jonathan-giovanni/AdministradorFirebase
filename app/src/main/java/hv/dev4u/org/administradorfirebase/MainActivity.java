package hv.dev4u.org.administradorfirebase;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    //elementos para la lista
    ListView listView;
    AdaptadorProductos adaptadorProductos;
    List<Producto> listaProductos;

    //boton
    FloatingActionButton btnAgregar;

    //base de datos firebase
    public static CollectionReference dbProductos;
    public static StorageReference imgFirebase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //inicializando controles para lista
        listView        = findViewById(R.id.listaProducto);
        listaProductos  = new ArrayList<>();
        adaptadorProductos = new AdaptadorProductos(this,listaProductos);
        listView.setAdapter(adaptadorProductos);

        //inicializando boton
        btnAgregar      = findViewById(R.id.btnAgregar);
        btnAgregar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nuevoProducto();
            }
        });

        //inicalizando la base de datos
        dbProductos     = FirebaseFirestore.getInstance().collection("productos");
        //inicializando el almacenamiento en firebase
        imgFirebase     = FirebaseStorage.getInstance().getReference("imagenes_productos");



        //cada cambioImagen en la bd se llama a actualizar datos
        dbProductos.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                actualizarDatos(queryDocumentSnapshots.getDocumentChanges());
            }
        });




        //menu que se despliega sobre elemento de la lista
        registerForContextMenu(listView);
        //en caso de que seleccione un elemento de la lista
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Producto producto = listaProductos.get(i);
                editarProducto(producto);
            }
        });
    }

    //agregar dato nuevo
    private void nuevoProducto(){
        Intent intent = new Intent(this,ProductoActivity.class);
        startActivity(intent);
    }

    //cuando sostengo el touch sobre un elemento de la lista se inicaliza el menu de opciones
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        //si el control establecido es la lista
        if (v.getId()==R.id.listaProducto) {
            //obtengo informacion sobre el item
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            //obtengo el producto apartir de la posicion guardada en info
            Producto producto = listaProductos.get(info.position);
            //como titulo del menu se establece el nombre del producto
            menu.setHeaderTitle(  producto.getNombre() );
            //inflar el menu
            MenuInflater inflater = getMenuInflater();
            //establecer el layout
            inflater.inflate(R.menu.menu_opciones_item, menu);
        }
    }

    //cuando selecciono un elemento del menu
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        Producto producto = listaProductos.get(info.position);

        //para casos de editar o borrar
        switch (item.getItemId()){
            case R.id.item_editar:
                //en caso de editar
                editarProducto(producto);
                return true;
            case R.id.item_borrar:
                borrarProducto(producto);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    //para el caso de editar se lanza un activity
    private void editarProducto(Producto producto){
        Intent intent = new Intent(this,ProductoActivity.class);
        intent.putExtra("PRODUCTO",producto);
        startActivity(intent);
    }

    //para el caso de borrar se pregunta en un dialogo si esta seguro de borrar
    private void borrarProducto(final Producto producto){
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Desea eliminar el producto : " + producto.getNombre()+" ?")
                .setPositiveButton("Si", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //en caso de que seleccione si entonces se procede a borrar de firebase
                        borrarProductoFirebase(producto);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    //se borrar el elemento producto de la bd en firebase
    private void borrarProductoFirebase(Producto producto){
        //obtengo el documento para esto utilizo el id unico como parametro
        //se procede a ejecutar dele y luego se agregan listeners en caso de que sea correcto o haya error
        dbProductos.document(producto.getId_producto())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Elemento eliminado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Ha ocurrido un error al intentar eliminar", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void actualizarDatos(List<DocumentChange> cambios){
        //para cada documento cambiado
        for(DocumentChange document_changed: cambios){
            //obtengo el documento
            DocumentSnapshot document = document_changed.getDocument();
            //obtengo la posicion del producto basado en el Id
            int posicion = posicionProducto(document.getId());
            //si el documento fue eliminado
            if(document_changed.getType()==DocumentChange.Type.REMOVED){
                //se elimina de la lista tambien
                listaProductos.remove(posicion);
            }else {
                //obtengo un objeto producto del documento
                Producto producto = getProducto(document);
                //si la posicion es mayor a cero es por que existe en la lista y se actualiza
                if (posicion >= 0) {
                    listaProductos.set(posicion, producto);
                } else {
                    //si no , es por que es un elemento nuevo
                    listaProductos.add(producto);
                }
            }
            Log.d("LISTA FIREBASE","Actualizada "+document.getId()+ " "+document.getData().values());
        }
        //notifico al adaptador de los cambios
        adaptadorProductos.notifyDataSetChanged();
        Toast.makeText(this, "Datos actualizados", Toast.LENGTH_SHORT).show();
    }



    //obtengo la posicion del producto basado en el id
    private int posicionProducto(String id){
        for(Producto producto : listaProductos) {
            if(producto.id_producto.equals(id)) {
                return listaProductos.indexOf(producto);
            }
        }
        return -1;
    }

    //obtengo objeto Producto basado en los parametros que hay en el documento de la BD
    private Producto getProducto(DocumentSnapshot doc){
        String id       = doc.getId();
        String nombre   = doc.getString("nombre");
        String precio   = doc.getString("precio");
        return new Producto(id,nombre,precio);
    }
}
